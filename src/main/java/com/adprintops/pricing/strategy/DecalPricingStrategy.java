package com.adprintops.pricing.strategy;

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
import java.util.Optional;

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
        BigDecimal width = request.widthM() != null ? request.widthM() : new BigDecimal("1.0");
        BigDecimal height = request.heightM() != null ? request.heightM() : new BigDecimal("1.0");
        int quantity = request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1;
        boolean hasLamination = Boolean.TRUE.equals(request.hasLamination());

        BigDecimal realSingleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);
        BigDecimal billableSingleArea = realSingleArea;

        // Roll fitting with rolls: 0.9m, 1.0m, 1.2m, 1.5m
        if (realSingleArea.compareTo(new BigDecimal("0.5")) >= 0) {
            BigDecimal best = null;
            for (BigDecimal roll : List.of(new BigDecimal("0.9"), BigDecimal.ONE, new BigDecimal("1.2"), new BigDecimal("1.5"))) {
                if (width.compareTo(roll) <= 0) best = best == null ? roll.multiply(height) : best.min(roll.multiply(height));
                if (height.compareTo(roll) <= 0) best = best == null ? roll.multiply(width) : best.min(roll.multiply(width));
            }
            if (best != null) billableSingleArea = best.setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal totalAreaSqm = billableSingleArea.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_UP);

        // Determine rate per sqm based on updated rules:
        // Area < 0.5m²: 130k | Area < 3m²: 130k | Area >= 3m²: 110k | Area >= 5m²: 100k | Area >= 10m²: 90k | Area >= 15m²: 80k
        BigDecimal baseRate;
        String ruleName;
        if (totalAreaSqm.compareTo(new BigDecimal("15.0")) >= 0) {
            baseRate = new BigDecimal("80000");
            ruleName = "Decal in >= 15m² (80k/m²)";
        } else if (totalAreaSqm.compareTo(new BigDecimal("10.0")) >= 0) {
            baseRate = new BigDecimal("90000");
            ruleName = "Decal in >= 10m² (90k/m²)";
        } else if (totalAreaSqm.compareTo(new BigDecimal("5.0")) >= 0) {
            baseRate = new BigDecimal("100000");
            ruleName = "Decal in >= 5m² (100k/m²)";
        } else if (totalAreaSqm.compareTo(new BigDecimal("3.0")) >= 0) {
            baseRate = new BigDecimal("110000");
            ruleName = "Decal in >= 3m² (110k/m²)";
        } else if (billableSingleArea.compareTo(new BigDecimal("0.5")) < 0) {
            baseRate = new BigDecimal("130000");
            ruleName = "Decal in dôi khổ < 0.5m² (130k/m²)";
        } else {
            baseRate = new BigDecimal("130000");
            ruleName = "Decal in < 3m² (130k/m²)";
        }

        String matCode = (request.materialCode() != null && !request.materialCode().isBlank())
                ? request.materialCode().toLowerCase() : "thuong";

        Optional<PricingMaterial> matOpt = pricingMaterialRepository
                .findByCategoryCodeAndMaterialCodeAndActiveTrue("DECAL", matCode);

        BigDecimal multiplier = matOpt.map(PricingMaterial::getMultiplier).orElse(BigDecimal.ONE);
        String matName = matOpt.map(PricingMaterial::getMaterialName).orElse("Decal in");
        if ("thuong".equals(matCode) || "decal".equals(matCode) || "in".equals(matCode)) {
            matName = "Decal in";
        }

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();
        appliedRules.add(ruleName);

        BigDecimal printCost = billableSingleArea.multiply(baseRate).multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
        lineItems.add(new LineItem("PRINT", "In " + matName + " (" + baseRate + "đ/m²)", printCost));

        BigDecimal laminationCost = BigDecimal.ZERO;
        if (hasLamination) {
            BigDecimal laminationFeeRate = new BigDecimal("50000");
            laminationCost = billableSingleArea.multiply(laminationFeeRate).setScale(0, RoundingMode.HALF_UP);
            lineItems.add(new LineItem("LAMINATION", "Phí cán màng (" + laminationFeeRate + "đ/m²)", laminationCost));
            appliedRules.add("DECAL_LAMINATION");
        }

        BigDecimal singleUnitPrice = printCost.add(laminationCost).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);

        String note = matName + " | Diện tích: " + realSingleArea + "m²" + (hasLamination ? " | Cán màng" : "");

        return new CalculatePriceResponse(
                "DECAL", false, realSingleArea, totalAreaSqm, baseRate.multiply(multiplier), laminationCost, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
