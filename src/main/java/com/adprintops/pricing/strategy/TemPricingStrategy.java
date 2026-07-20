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
public class TemPricingStrategy implements PricingStrategy {

    @Override
    public String getCategoryCode() {
        return "TEM";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        BigDecimal width = request.widthM() != null ? request.widthM() : new BigDecimal("0.1");
        BigDecimal height = request.heightM() != null ? request.heightM() : new BigDecimal("0.1");
        int quantity = request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1;

        boolean isDieCut = Boolean.TRUE.equals(request.hasDieCut()) ||
                (request.materialCode() != null && request.materialCode().toLowerCase().contains("be"));

        BigDecimal realSingleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);
        BigDecimal realTotalArea = realSingleArea.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_UP);

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        BigDecimal ratePerSqm;
        BigDecimal totalPrice;
        BigDecimal singleUnitPrice;

        if (isDieCut) {
            // Tem bế: Nửa mét 100k, 1m² 140k, 2m² 260k, 5m² 550k
            if (realTotalArea.compareTo(new BigDecimal("0.5")) <= 0) {
                totalPrice = new BigDecimal("100000");
                ratePerSqm = new BigDecimal("200000");
                appliedRules.add("TEM_BE_0.5M2_100K");
            } else if (realTotalArea.compareTo(BigDecimal.ONE) <= 0) {
                totalPrice = new BigDecimal("140000");
                ratePerSqm = new BigDecimal("140000");
                appliedRules.add("TEM_BE_1M2_140K");
            } else if (realTotalArea.compareTo(new BigDecimal("2.0")) <= 0) {
                totalPrice = new BigDecimal("260000");
                ratePerSqm = new BigDecimal("130000");
                appliedRules.add("TEM_BE_2M2_260K");
            } else if (realTotalArea.compareTo(new BigDecimal("5.0")) <= 0) {
                totalPrice = new BigDecimal("550000");
                ratePerSqm = new BigDecimal("110000");
                appliedRules.add("TEM_BE_5M2_550K");
            } else {
                ratePerSqm = new BigDecimal("100000");
                totalPrice = realTotalArea.multiply(ratePerSqm).setScale(0, RoundingMode.HALF_UP);
                appliedRules.add("TEM_BE_ABOVE_5M2_100K");
            }
            singleUnitPrice = totalPrice.divide(BigDecimal.valueOf(quantity), 0, RoundingMode.HALF_UP);
            lineItems.add(new LineItem("PRINT_BE", "Tem bế hình (Tổng diện tích: " + realTotalArea + "m², SL tem: " + quantity + " tem)", totalPrice));
        } else {
            // Tem không bế * 100k
            ratePerSqm = new BigDecimal("100000");
            BigDecimal billableArea = realTotalArea.compareTo(new BigDecimal("0.5")) < 0 ? new BigDecimal("0.5") : realTotalArea;
            totalPrice = billableArea.multiply(ratePerSqm).setScale(0, RoundingMode.HALF_UP);
            singleUnitPrice = totalPrice.divide(BigDecimal.valueOf(quantity), 0, RoundingMode.HALF_UP);

            appliedRules.add("TEM_KHONG_BE_100K");
            lineItems.add(new LineItem("PRINT_NO_BE", "Tem không bế (Tổng diện tích: " + billableArea + "m², SL tem: " + quantity + " tem)", totalPrice));
        }

        String note = (isDieCut ? "Tem bế hình" : "Tem không bế") + " | Tổng diện tích: " + realTotalArea + "m² | Số lượng tem: " + quantity + " tem";

        return new CalculatePriceResponse(
                "TEM", false, realSingleArea, realTotalArea, ratePerSqm, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
