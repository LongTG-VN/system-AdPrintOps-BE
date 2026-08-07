package com.adprintops.pricing.strategy;

import com.adprintops.pricing.dto.CalculatePriceRequest;
import com.adprintops.pricing.dto.CalculatePriceResponse;
import com.adprintops.pricing.dto.LineItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class CatPricingStrategy implements PricingStrategy {

    @Override
    public String getCategoryCode() {
        return "CAT";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        BigDecimal width = request.widthM() != null ? request.widthM() : new BigDecimal("1.0");
        BigDecimal height = request.heightM() != null ? request.heightM() : new BigDecimal("1.0");
        int quantity = request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1;
        String mode = request.cutMode() != null ? request.cutMode().toLowerCase() : "chuan";
        String matCode = request.materialCode() != null ? request.materialCode().toLowerCase() : "decal_si";

        BigDecimal realSingleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);
        BigDecimal totalArea = realSingleArea.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_UP);

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        BigDecimal singleUnitPrice;
        BigDecimal totalPrice;
        BigDecimal ratePerSqm;
        String note;
        String matLabel;

        BigDecimal[] availableRolls;

        // Map material code to specific rate, roll widths & label from photo
        if ("decal_pq".equals(matCode) || "da_quang".equals(matCode)) {
            ratePerSqm = new BigDecimal("200000");
            matLabel = "Decal PQ khổ 60 (Dạ quang - 1.5 năm)";
            availableRolls = new BigDecimal[]{new BigDecimal("0.6")};
        } else if ("decal_in_be".equals(matCode) || "in_be".equals(matCode)) {
            ratePerSqm = new BigDecimal("200000");
            matLabel = "Decal in bế khổ 90/100/120 (8 tháng - 1 năm)";
            availableRolls = new BigDecimal[]{new BigDecimal("0.9"), new BigDecimal("1.0"), new BigDecimal("1.2")};
        } else if ("decal_tot_1".equals(matCode) || "decal_tot".equals(matCode)) {
            ratePerSqm = new BigDecimal("150000");
            matLabel = "Decal tốt 1 lớp khổ 120 (2.5 năm - Nền trắng)";
            availableRolls = new BigDecimal[]{new BigDecimal("1.2")};
        } else if ("decal_tot_2".equals(matCode)) {
            ratePerSqm = new BigDecimal("300000");
            matLabel = "Decal tốt 2 lớp khổ 120 (2.5 năm)";
            availableRolls = new BigDecimal[]{new BigDecimal("1.2")};
        } else {
            // Default: Decal si (3 tháng) khổ 60 x 100k
            ratePerSqm = new BigDecimal("100000");
            matLabel = "Decal si (3 tháng) khổ 60";
            availableRolls = new BigDecimal[]{new BigDecimal("0.6")};
        }

        if ("vien".equals(mode)) {
            BigDecimal maxSide = request.maxSideM() != null ? request.maxSideM() : width.max(height);
            BigDecimal perSheetPrice;

            if (maxSide.compareTo(new BigDecimal("0.1")) < 0) perSheetPrice = new BigDecimal("60000");
            else if (maxSide.compareTo(new BigDecimal("0.3")) < 0) perSheetPrice = new BigDecimal("80000");
            else if (maxSide.compareTo(new BigDecimal("0.4")) < 0) perSheetPrice = new BigDecimal("160000");
            else if (maxSide.compareTo(new BigDecimal("0.6")) < 0) perSheetPrice = new BigDecimal("200000");
            else if (maxSide.compareTo(new BigDecimal("0.7")) < 0) perSheetPrice = new BigDecimal("245000");
            else if (maxSide.compareTo(new BigDecimal("0.8")) < 0) perSheetPrice = new BigDecimal("260000");
            else perSheetPrice = new BigDecimal("350000");

            singleUnitPrice = perSheetPrice;
            totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("CAT_MODE_VIEN_TIER");
            lineItems.add(new LineItem("CUT_VIEN", "Cắt viền " + matLabel + " (Cạnh max: " + maxSide + "m @ " + perSheetPrice + "đ/tấm)", totalPrice));
            note = "Cắt Decal Viền | " + matLabel + " | Cạnh max " + maxSide + "m";
        } else if ("le".equals(mode)) {
            int tacRoll = request.rollWidthTac() != null && request.rollWidthTac() > 6 ? 10 : 6;
            BigDecimal pricePerTac = (tacRoll == 10) ? new BigDecimal("15000") : new BigDecimal("10000");
            BigDecimal tacCount = request.sheetCount() != null && request.sheetCount() > 0
                    ? BigDecimal.valueOf(request.sheetCount())
                    : height.multiply(new BigDecimal("10"));

            singleUnitPrice = tacCount.multiply(pricePerTac).setScale(0, RoundingMode.HALF_UP);
            totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("CAT_MODE_LE_ROLL_TAC");
            lineItems.add(new LineItem("CUT_LE", "Cắt decal lẻ " + matLabel + " khổ " + tacRoll + " tấc (" + tacCount + " tấc @ " + pricePerTac + "đ/tấc)", totalPrice));
            note = "Cắt Decal Lẻ | " + matLabel + " | Khổ " + tacRoll + " tấc";
        } else {
            // Cut standard per m² with ROLL FITTING & AREA ROUNDING (<= 0.10 => 0.20, > 0.10 => làm tròn lên mỗi 0.10m²)
            BigDecimal rawRollArea = calculateBillableRollArea(width, height, availableRolls);
            BigDecimal billableRollArea = DecalPricingStrategy.roundUpAreaToTenths(rawRollArea);

            singleUnitPrice = billableRollArea.multiply(ratePerSqm).setScale(0, RoundingMode.HALF_UP);
            totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("CAT_MATERIAL_" + matCode.toUpperCase() + "_ROLL_FITTING");
            lineItems.add(new LineItem("CUT_CHUAN", "Cắt " + matLabel + " (Dô khổ/làm tròn: " + billableRollArea + "m² @ " + ratePerSqm + "đ/m²)", totalPrice));
            note = "Cắt Decal | " + matLabel + " | Kích thước: " + width + "m x " + height + "m (Làm tròn diện tích: " + billableRollArea + "m²)";
        }

        BigDecimal billableTotalArea = DecalPricingStrategy.roundUpAreaToTenths(calculateBillableRollArea(width, height, availableRolls)).multiply(BigDecimal.valueOf(quantity));

        return new CalculatePriceResponse(
                "CAT", false, realSingleArea, billableTotalArea, ratePerSqm, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }

    private BigDecimal calculateBillableRollArea(BigDecimal width, BigDecimal height, BigDecimal[] availableRolls) {
        BigDecimal minArea = new BigDecimal("999999");

        for (BigDecimal r : availableRolls) {
            // Orientation 1: width fits across roll r
            if (width.compareTo(r) <= 0) {
                BigDecimal area1 = r.multiply(height);
                if (area1.compareTo(minArea) < 0) {
                    minArea = area1;
                }
            }
            // Orientation 2: height fits across roll r
            if (height.compareTo(r) <= 0) {
                BigDecimal area2 = r.multiply(width);
                if (area2.compareTo(minArea) < 0) {
                    minArea = area2;
                }
            }
        }

        // Fallback if item is larger than available rolls
        if (minArea.compareTo(new BigDecimal("999999")) == 0) {
            minArea = width.multiply(height);
        }

        return minArea.setScale(4, RoundingMode.HALF_UP);
    }
}
