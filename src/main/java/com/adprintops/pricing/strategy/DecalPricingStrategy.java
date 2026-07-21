package com.adprintops.pricing.strategy;

import com.adprintops.pricing.PricingConfigurationException;
import com.adprintops.pricing.domain.PricingMaterial;
import com.adprintops.pricing.domain.PricingMaterialRepository;
import com.adprintops.pricing.domain.PricingRule;
import com.adprintops.pricing.domain.PricingRuleRepository;
import com.adprintops.pricing.dto.CalculatePriceRequest;
import com.adprintops.pricing.dto.CalculatePriceResponse;
import com.adprintops.pricing.dto.LineItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class DecalPricingStrategy implements PricingStrategy {

    private final PricingRuleRepository pricingRuleRepository;
    private final PricingMaterialRepository pricingMaterialRepository;

    public DecalPricingStrategy(PricingRuleRepository pricingRuleRepository,
                                PricingMaterialRepository pricingMaterialRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.pricingMaterialRepository = pricingMaterialRepository;
    }

    @Override
    public String getCategoryCode() {
        return "DECAL";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        BigDecimal width = request.widthM();
        BigDecimal height = request.heightM();
        int quantity = request.quantity();
        boolean hasLamination = Boolean.TRUE.equals(request.hasLamination());

        BigDecimal realSingleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);
        BigDecimal billableSingleArea = realSingleArea;

        // Source contract: jobs below 0.1m² use real area; all larger jobs try 0.9/1.0/1.2m rolls.
        if (realSingleArea.compareTo(new BigDecimal("0.1")) >= 0) {
            BigDecimal best = null;
            for (BigDecimal roll : List.of(new BigDecimal("0.9"), BigDecimal.ONE, new BigDecimal("1.2"))) {
                if (width.compareTo(roll) <= 0) best = best == null ? roll.multiply(height) : best.min(roll.multiply(height));
                if (height.compareTo(roll) <= 0) best = best == null ? roll.multiply(width) : best.min(roll.multiply(width));
            }
            if (best != null) billableSingleArea = best.setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal totalAreaSqm = billableSingleArea.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_UP);

        List<PricingRule> matchingRules = pricingRuleRepository.findMatchingRules("DECAL", billableSingleArea);
        if (matchingRules.isEmpty()) {
            throw new PricingConfigurationException(
                    "Chưa có quy tắc giá DECAL cho diện tích tính tiền " + billableSingleArea + "m²"
            );
        }
        PricingRule rule = matchingRules.getFirst();

        String matCode = (request.materialCode() != null && !request.materialCode().isBlank())
                ? request.materialCode().toLowerCase() : "thuong";

        PricingMaterial material = pricingMaterialRepository
                .findByCategoryCodeAndMaterialCodeAndActiveTrue("DECAL", matCode)
                .orElseThrow(() -> new PricingConfigurationException(
                        "Chưa có vật liệu DECAL đang hoạt động với mã " + matCode
                ));

        BigDecimal effectiveRate = rule.getPricePerSqm()
                .multiply(material.getMultiplier())
                .add(material.getBasePrice());
        String matName = material.getMaterialName();
        if ("thuong".equals(matCode) || "decal".equals(matCode) || "in".equals(matCode)) {
            matName = "Decal in";
        }

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();
        appliedRules.add(rule.getRuleName());

        BigDecimal printCost = billableSingleArea.multiply(effectiveRate).setScale(0, RoundingMode.HALF_UP);
        lineItems.add(new LineItem("PRINT", "In " + matName + " (" + effectiveRate + "đ/m²)", printCost));

        BigDecimal laminationCost = BigDecimal.ZERO;
        if (hasLamination) {
            BigDecimal laminationFeeRate = rule.getLaminationFeePerSqm();
            laminationCost = billableSingleArea.multiply(laminationFeeRate).setScale(0, RoundingMode.HALF_UP);
            lineItems.add(new LineItem("LAMINATION", "Phí cán màng (" + laminationFeeRate + "đ/m²)", laminationCost));
            appliedRules.add("DECAL_LAMINATION");
        }

        BigDecimal singleUnitPrice = printCost.add(laminationCost).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);

        String note = matName + " | Diện tích: " + realSingleArea + "m²" + (hasLamination ? " | Cán màng" : "");

        return new CalculatePriceResponse(
                "DECAL", false, realSingleArea, totalAreaSqm, effectiveRate, laminationCost, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
