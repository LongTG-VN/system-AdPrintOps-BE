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
public class HastagPricingStrategy implements PricingStrategy {

    @Override
    public String getCategoryCode() {
        return "HASTAG";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        BigDecimal width = request.widthM() != null ? request.widthM() : new BigDecimal("0.20");
        BigDecimal height = request.heightM() != null ? request.heightM() : new BigDecimal("0.30");
        int quantity = request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1;
        String thickness = request.hastagThickness() != null ? request.hastagThickness().toLowerCase() : "3li";
        boolean hasCnc = Boolean.TRUE.equals(request.hasCncCut());

        int wCm = (int) Math.round(width.doubleValue() * 100);
        int hCm = (int) Math.round(height.doubleValue() * 100);
        int minCm = Math.min(wCm, hCm);
        int maxCm = Math.max(wCm, hCm);

        boolean is5li = "5li".equals(thickness) || "5mm".equals(thickness);

        BigDecimal basePrice;
        String sizeTierLabel;

        if (minCm <= 15 && maxCm <= 20) {
            basePrice = is5li ? new BigDecimal("65000") : new BigDecimal("55000");
            sizeTierLabel = "10-15x20cm";
        } else if (minCm <= 15 && maxCm <= 35) {
            basePrice = is5li ? new BigDecimal("75000") : new BigDecimal("65000");
            sizeTierLabel = "15x30-35cm";
        } else if (minCm <= 20 && maxCm <= 30) {
            basePrice = is5li ? new BigDecimal("85000") : new BigDecimal("70000");
            sizeTierLabel = "20x30cm";
        } else {
            basePrice = is5li ? new BigDecimal("90000") : new BigDecimal("75000");
            sizeTierLabel = "20x35-40cm";
        }

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        String thickLabel = is5li ? "Formex 5li" : "Formex 3li";
        lineItems.add(new LineItem("HASTAG_BASE", "Hastag bảng tên cầm tay " + thickLabel + " (" + sizeTierLabel + ")", basePrice));
        appliedRules.add("HASTAG_" + (is5li ? "5LI" : "3LI") + "_" + sizeTierLabel);

        BigDecimal singleUnitPrice = basePrice;

        if (hasCnc) {
            BigDecimal cncFee = new BigDecimal("20000");
            singleUnitPrice = singleUnitPrice.add(cncFee);
            lineItems.add(new LineItem("CNC_CUT", "Gia công cắt CNC theo hình (+20k/cái)", cncFee));
            appliedRules.add("HASTAG_CNC_CUT");
        }

        // Quantity discount (>= 2c giảm 5k/cái, >= 4c giảm 10k/cái)
        BigDecimal discountPerItem = BigDecimal.ZERO;
        if (quantity >= 4) {
            discountPerItem = new BigDecimal("10000");
            appliedRules.add("HASTAG_QTY_DISCOUNT_10K_PER_ITEM");
        } else if (quantity >= 2) {
            discountPerItem = new BigDecimal("5000");
            appliedRules.add("HASTAG_QTY_DISCOUNT_5K_PER_ITEM");
        }

        if (discountPerItem.compareTo(BigDecimal.ZERO) > 0) {
            singleUnitPrice = singleUnitPrice.subtract(discountPerItem);
            lineItems.add(new LineItem("QTY_DISCOUNT", "Chiết khấu số lượng " + quantity + " cái (-" + discountPerItem + "đ/cái)", discountPerItem.multiply(BigDecimal.valueOf(quantity)).negate()));
        }

        BigDecimal totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal singleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);

        String note = "Hastag " + thickLabel + " | Kích thước: " + wCm + "x" + hCm + "cm (" + quantity + " cái @ " + singleUnitPrice + "đ/cái)";

        return new CalculatePriceResponse(
                "HASTAG", false, singleArea, singleArea.multiply(BigDecimal.valueOf(quantity)), singleUnitPrice, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
