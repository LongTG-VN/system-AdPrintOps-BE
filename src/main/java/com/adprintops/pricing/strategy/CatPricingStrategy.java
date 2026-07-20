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

        // Map material code to specific rate & label
        if ("decal_pq".equals(matCode) || "da_quang".equals(matCode)) {
            ratePerSqm = new BigDecimal("200000");
            matLabel = "Decal PQ khổ 60 (Dạ quang - 1.5 năm)";
        } else if ("decal_in_be".equals(matCode) || "in_be".equals(matCode)) {
            ratePerSqm = new BigDecimal("200000");
            matLabel = "Decal in bế khổ 90/100/120 (8 tháng - 1 năm)";
        } else if ("decal_tot_1".equals(matCode) || "decal_tot".equals(matCode)) {
            ratePerSqm = new BigDecimal("150000");
            matLabel = "Decal tốt 1 lớp khổ 120 (2.5 năm - Nền trắng)";
        } else if ("decal_tot_2".equals(matCode)) {
            ratePerSqm = new BigDecimal("300000");
            matLabel = "Decal tốt 2 lớp khổ 120 (2.5 năm)";
        } else {
            // Default: Decal si (3 tháng) khổ 60
            ratePerSqm = new BigDecimal("100000");
            matLabel = "Decal si (3 tháng) khổ 60";
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
            // Cut standard per m² with selected material rate
            singleUnitPrice = realSingleArea.multiply(ratePerSqm).setScale(0, RoundingMode.HALF_UP);
            totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("CAT_MATERIAL_" + matCode.toUpperCase());
            lineItems.add(new LineItem("CUT_CHUAN", "Cắt " + matLabel + " (" + ratePerSqm + "đ/m²)", totalPrice));
            note = "Cắt Decal | " + matLabel + " (" + ratePerSqm + "đ/m²)";
        }

        return new CalculatePriceResponse(
                "CAT", false, realSingleArea, totalArea, ratePerSqm, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
