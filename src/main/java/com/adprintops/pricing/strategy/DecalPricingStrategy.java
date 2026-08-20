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
        BigDecimal width = request.widthM();
        BigDecimal height = request.heightM();
        int quantity = request.quantity();
        boolean hasLamination = Boolean.TRUE.equals(request.hasLamination());

        BigDecimal realSingleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);
        BigDecimal billableSingleArea = realSingleArea;

        // Check small piece matrix lookup first (Decal = Lụa for small dimensions)
        BigDecimal smallPiecePrice = SmallPiecePricingMatrix.findPrice(width, height, quantity);
        if (smallPiecePrice != null) {
            BigDecimal singleUnitPrice = smallPiecePrice.divide(BigDecimal.valueOf(quantity), 0, RoundingMode.HALF_UP);
            BigDecimal laminationCost = BigDecimal.ZERO;
            if (hasLamination) {
                BigDecimal laminationFeeRate = new BigDecimal("50000");
                laminationCost = realSingleArea.multiply(laminationFeeRate).setScale(0, RoundingMode.HALF_UP);
            }
            BigDecimal totalPrice = smallPiecePrice.add(laminationCost.multiply(BigDecimal.valueOf(quantity))).setScale(0, RoundingMode.HALF_UP);

            List<LineItem> lineItems = new ArrayList<>();
            List<String> appliedRules = new ArrayList<>();
            appliedRules.add("DECAL_SMALL_PIECE_MATRIX");
            lineItems.add(new LineItem("PRINT", "In Decal tấm nhỏ khoán (" + width + "m x " + height + "m)", smallPiecePrice));
            if (hasLamination) {
                lineItems.add(new LineItem("LAMINATION", "Phí cán màng (+50k/m²)", laminationCost.multiply(BigDecimal.valueOf(quantity))));
            }
            String note = "Decal in tấm nhỏ | Kích thước: " + width + "m x " + height + "m (" + quantity + " tấm @ " + totalPrice + "đ)";

            return new CalculatePriceResponse(
                    "DECAL", false, realSingleArea, realSingleArea.multiply(BigDecimal.valueOf(quantity)), new BigDecimal("120000"), laminationCost, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
            );
        }

        String matCode = (request.materialCode() != null && !request.materialCode().isBlank())
                ? request.materialCode().toLowerCase() : "thuong";

        // Decal đục (sữa): rolls 0.9m, 1.0m, 1.07m, 1.27m, 1.52m
        // Decal trong: rolls 0.9m, 1.0m, 1.07m, 1.27m (Decal trong KHÔNG CÓ CÂY 150cm!)
        List<BigDecimal> rolls = "trong".equals(matCode)
                ? List.of(new BigDecimal("0.90"), BigDecimal.ONE, new BigDecimal("1.07"), new BigDecimal("1.27"))
                : List.of(new BigDecimal("0.90"), BigDecimal.ONE, new BigDecimal("1.07"), new BigDecimal("1.27"), new BigDecimal("1.52"));

        BigDecimal matchedRoll = null;
        if (realSingleArea.compareTo(new BigDecimal("0.1")) >= 0) {
            BigDecimal bestArea = null;
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

        List<PricingRule> matchingRules = pricingRuleRepository.findMatchingRules("DECAL", totalAreaSqm);
        BigDecimal baseRate;
        String ruleName;

        if (!matchingRules.isEmpty()) {
            PricingRule rule = matchingRules.getFirst();
            baseRate = rule.getPricePerSqm();
            ruleName = rule.getRuleName();
        } else {
            // Fallback calculation matching shop's total order area tiers
            if (totalAreaSqm.compareTo(new BigDecimal("15.0")) >= 0) {
                baseRate = new BigDecimal("80000");
                ruleName = "DECAL_TIER_GE_15M2";
            } else if (totalAreaSqm.compareTo(new BigDecimal("10.0")) >= 0) {
                baseRate = new BigDecimal("90000");
                ruleName = "DECAL_TIER_GE_10M2";
            } else if (totalAreaSqm.compareTo(new BigDecimal("5.0")) >= 0) {
                baseRate = new BigDecimal("100000");
                ruleName = "DECAL_TIER_GE_5M2";
            } else if (totalAreaSqm.compareTo(new BigDecimal("3.0")) >= 0) {
                baseRate = new BigDecimal("110000");
                ruleName = "DECAL_TIER_GE_3M2";
            } else if (totalAreaSqm.compareTo(new BigDecimal("0.5")) >= 0) {
                baseRate = new BigDecimal("130000");
                ruleName = "DECAL_TIER_UNDER_3M2";
            } else {
                baseRate = new BigDecimal("200000");
                ruleName = "DECAL_TIER_UNDER_0.5M2";
            }
        }

        // Volume discounts for large total area (>= 3m², >= 5m², >= 10m², >= 15m²)
        if (totalAreaSqm.compareTo(new BigDecimal("15.0")) >= 0) {
            baseRate = baseRate.min(new BigDecimal("80000"));
            ruleName = "DECAL_VO_15M2";
        } else if (totalAreaSqm.compareTo(new BigDecimal("10.0")) >= 0) {
            baseRate = baseRate.min(new BigDecimal("90000"));
            ruleName = "DECAL_VO_10M2";
        } else if (totalAreaSqm.compareTo(new BigDecimal("5.0")) >= 0) {
            baseRate = baseRate.min(new BigDecimal("100000"));
            ruleName = "DECAL_VO_5M2";
        } else if (totalAreaSqm.compareTo(new BigDecimal("3.0")) >= 0) {
            baseRate = baseRate.min(new BigDecimal("110000"));
            ruleName = "DECAL_VO_3M2";
        }

        BigDecimal multiplier = BigDecimal.ONE;
        BigDecimal extraBasePrice = BigDecimal.ZERO;
        String matName = "trong".equals(matCode) ? "Decal in trong" : "Decal in đục";

        Optional<PricingMaterial> optMaterial = pricingMaterialRepository.findByCategoryCodeAndMaterialCodeAndActiveTrue("DECAL", matCode);
        if (optMaterial.isPresent()) {
            matName = optMaterial.get().getMaterialName();
            if (optMaterial.get().getMultiplier() != null) {
                multiplier = optMaterial.get().getMultiplier();
            }
            if (optMaterial.get().getBasePrice() != null) {
                extraBasePrice = optMaterial.get().getBasePrice();
            }
        }

        BigDecimal effectiveRate = baseRate.multiply(multiplier).add(extraBasePrice);

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
