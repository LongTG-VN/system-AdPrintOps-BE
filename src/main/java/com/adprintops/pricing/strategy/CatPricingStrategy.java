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

        // Map material code to specific rate, roll widths & label from user specifications
        if ("decal_pq".equals(matCode) || "da_quang".equals(matCode)) {
            ratePerSqm = new BigDecimal("150000"); // Decal PQ khổ 60 x 150k
            matLabel = "Decal PQ khổ 60 (Dạ quang)";
            availableRolls = new BigDecimal[]{new BigDecimal("0.6")};
        } else if ("decal_tot".equals(matCode) || "decal_tot_1".equals(matCode)) {
            ratePerSqm = new BigDecimal("150000"); // Decal tốt khổ 120 x 150k
            matLabel = "Decal tốt khổ 120";
            availableRolls = new BigDecimal[]{new BigDecimal("1.2")};
        } else if ("decal_in_be".equals(matCode) || "in_be".equals(matCode)) {
            ratePerSqm = new BigDecimal("200000"); // Decal in bế khổ 100, 120 x 200k
            matLabel = "Decal in bế khổ 100/120";
            availableRolls = new BigDecimal[]{new BigDecimal("1.0"), new BigDecimal("1.2")};
        } else if ("decal_uv".equals(matCode) || "decal_tot_2".equals(matCode)) {
            ratePerSqm = new BigDecimal("300000"); // Decal UV khổ 100, 120 x 300k
            matLabel = "Decal UV khổ 100/120";
            availableRolls = new BigDecimal[]{new BigDecimal("1.0"), new BigDecimal("1.2")};
        } else {
            // Default: Decal màu thường khổ 60 x 100k
            ratePerSqm = new BigDecimal("100000");
            matLabel = "Decal màu thường khổ 60";
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
            // Cut standard per m² with ROLL FITTING & AREA ROUNDING
            RollFitDetail fit = calculateRollFittingDetails(width, height, availableRolls);
            BigDecimal billableRollArea = DecalPricingStrategy.roundUpAreaToTenths(fit.rawArea());

            singleUnitPrice = billableRollArea.multiply(ratePerSqm).setScale(0, RoundingMode.HALF_UP);
            totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("CAT_MATERIAL_" + matCode.toUpperCase() + "_ROLL_FITTING");
            lineItems.add(new LineItem("CUT_CHUAN", "Cắt " + matLabel + " (Vô khổ " + fit.rollWidth() + "m x " + fit.cutLength() + "m = " + billableRollArea + "m² @ " + ratePerSqm + "đ/m²)", totalPrice));
            note = "Cắt Decal | " + matLabel + " | Kích thước: " + width + "m x " + height + "m (Vô khổ cuộn " + fit.rollWidth() + "m x " + fit.cutLength() + "m = " + billableRollArea + "m²)";
        }

        RollFitDetail fitDetail = calculateRollFittingDetails(width, height, availableRolls);
        BigDecimal billableTotalArea = DecalPricingStrategy.roundUpAreaToTenths(fitDetail.rawArea()).multiply(BigDecimal.valueOf(quantity));

        return new CalculatePriceResponse(
                "CAT", false, realSingleArea, billableTotalArea, ratePerSqm, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }

    public record RollFitDetail(BigDecimal rollWidth, BigDecimal cutLength, BigDecimal rawArea) {}

    private RollFitDetail calculateRollFittingDetails(BigDecimal width, BigDecimal height, BigDecimal[] availableRolls) {
        BigDecimal minArea = new BigDecimal("999999");
        BigDecimal bestRoll = availableRolls[0];
        BigDecimal bestLength = width.min(height);

        for (BigDecimal r : availableRolls) {
            if (width.compareTo(r) <= 0) {
                BigDecimal area1 = r.multiply(height);
                if (area1.compareTo(minArea) < 0) {
                    minArea = area1;
                    bestRoll = r;
                    bestLength = height;
                }
            }
            if (height.compareTo(r) <= 0) {
                BigDecimal area2 = r.multiply(width);
                if (area2.compareTo(minArea) < 0) {
                    minArea = area2;
                    bestRoll = r;
                    bestLength = width;
                }
            }
        }

        if (minArea.compareTo(new BigDecimal("999999")) == 0) {
            BigDecimal maxR = availableRolls[availableRolls.length - 1];
            BigDecimal maxDim = width.max(height);
            bestRoll = maxR;
            bestLength = maxDim;
            minArea = maxR.multiply(maxDim);
        }

        return new RollFitDetail(bestRoll, bestLength.setScale(2, RoundingMode.HALF_UP), minArea.setScale(4, RoundingMode.HALF_UP));
    }
}
