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

        // Standard Decal print shop rolls: 0.9m, 1.0m, 1.07m, 1.27m, 1.52m
        BigDecimal matchedRoll = null;
        if (realSingleArea.compareTo(new BigDecimal("0.1")) >= 0) {
            BigDecimal bestArea = null;
            List<BigDecimal> rolls = List.of(
                    new BigDecimal("0.90"),
                    BigDecimal.ONE,
                    new BigDecimal("1.07"),
                    new BigDecimal("1.27"),
                    new BigDecimal("1.52")
            );
            for (BigDecimal roll : rolls) {
                if (width.compareTo(roll) <= 0) {
                    BigDecimal area = roll.multiply(height);
                    if (bestArea == null || area.compareTo(bestArea) < 0) {
                        bestArea = area;
                        matchedRoll = roll;
                    }
                }
                if (height.compareTo(roll) <= 0) {
                    BigDecimal area = roll.multiply(width);
                    if (bestArea == null || area.compareTo(bestArea) < 0) {
                        bestArea = area;
                        matchedRoll = roll;
                    }
                }
            }
            if (bestArea != null) billableSingleArea = bestArea.setScale(4, RoundingMode.HALF_UP);
        }

        // Apply area rounding rule (<= 0.10 => 0.20; > 0.10 => làm tròn lên mỗi 0.10m²)
        billableSingleArea = roundUpAreaToTenths(billableSingleArea);

        BigDecimal totalAreaSqm = billableSingleArea.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_UP);

        List<PricingRule> matchingRules = pricingRuleRepository.findMatchingRules("DECAL", billableSingleArea);
        if (matchingRules.isEmpty()) {
            throw new PricingConfigurationException(
                    "Chưa có quy tắc giá DECAL cho diện tích tính tiền " + billableSingleArea + "m²"
            );
        }
        PricingRule rule = matchingRules.getFirst();

        BigDecimal baseRate = rule.getPricePerSqm();
        String ruleName = rule.getRuleName();

        // Volume discounts for large total area (> 4m²)
        if (totalAreaSqm.compareTo(new BigDecimal("15.0")) >= 0) {
            baseRate = baseRate.min(new BigDecimal("80000"));
            ruleName = "DECAL_VO_15M2";
        } else if (totalAreaSqm.compareTo(new BigDecimal("10.0")) >= 0) {
            baseRate = baseRate.min(new BigDecimal("90000"));
            ruleName = "DECAL_VO_10M2";
        } else if (totalAreaSqm.compareTo(new BigDecimal("5.0")) >= 0) {
            baseRate = baseRate.min(new BigDecimal("100000"));
            ruleName = "DECAL_VO_5M2";
        }

        String matCode = (request.materialCode() != null && !request.materialCode().isBlank())
                ? request.materialCode().toLowerCase() : "thuong";

        PricingMaterial material = pricingMaterialRepository
                .findByCategoryCodeAndMaterialCodeAndActiveTrue("DECAL", matCode)
                .orElseThrow(() -> new PricingConfigurationException(
                        "Chưa có vật liệu DECAL đang hoạt động với mã " + matCode
                ));

        BigDecimal effectiveRate = baseRate
                .multiply(material.getMultiplier())
                .add(material.getBasePrice());
        String matName = material.getMaterialName();
        if ("thuong".equals(matCode) || "decal".equals(matCode) || "in".equals(matCode)) {
            matName = "Decal in";
        }

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();
        appliedRules.add(ruleName);

        BigDecimal printCost = billableSingleArea.multiply(effectiveRate).setScale(0, RoundingMode.HALF_UP);
        String rollLabel = matchedRoll != null ? " (Khổ cuộn " + matchedRoll + "m)" : "";
        lineItems.add(new LineItem("PRINT", "In " + matName + rollLabel + " (" + effectiveRate + "đ/m²)", printCost));

        BigDecimal laminationCost = BigDecimal.ZERO;
        if (hasLamination) {
            BigDecimal laminationFeeRate = new BigDecimal("50000");
            laminationCost = billableSingleArea.multiply(laminationFeeRate).setScale(0, RoundingMode.HALF_UP);
            lineItems.add(new LineItem("LAMINATION", "Phí cán màng (" + laminationFeeRate + "đ/m²)", laminationCost));
            appliedRules.add("DECAL_LAMINATION");
        }

        BigDecimal singleUnitPrice = printCost.add(laminationCost).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);

        String note = matName + " | In: " + width + "m x " + height + "m"
                + (matchedRoll != null ? " (Xếp khổ cuộn: " + matchedRoll + "m -> Tính: " + billableSingleArea + "m²)" : "")
                + (hasLamination ? " | Cán màng" : "");

        return new CalculatePriceResponse(
                "DECAL", false, realSingleArea, totalAreaSqm, effectiveRate, laminationCost, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }

    public static BigDecimal roundUpAreaToTenths(BigDecimal rawArea) {
        if (rawArea == null || rawArea.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("0.20");
        }
        if (rawArea.compareTo(new BigDecimal("0.10")) <= 0) {
            return new BigDecimal("0.20");
        }

        BigDecimal multiplied = rawArea.multiply(new BigDecimal("10"));
        BigDecimal ceiled = multiplied.setScale(0, RoundingMode.CEILING);
        BigDecimal rounded = ceiled.divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);

        if (rounded.compareTo(new BigDecimal("0.20")) < 0) {
            return new BigDecimal("0.20");
        }

        return rounded;
    }
}
