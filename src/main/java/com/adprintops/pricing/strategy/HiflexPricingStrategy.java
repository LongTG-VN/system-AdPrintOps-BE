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
public class HiflexPricingStrategy implements PricingStrategy {

    @Override
    public String getCategoryCode() {
        return "HIFLEX";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        BigDecimal width = request.widthM() != null ? request.widthM() : BigDecimal.ONE;
        BigDecimal height = request.heightM() != null ? request.heightM() : BigDecimal.ONE;
        int quantity = request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1;

        BigDecimal activeSingleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);
        BigDecimal totalAreaAllItems = activeSingleArea.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_UP);

        String type = request.hiflexType() != null ? request.hiflexType().toLowerCase() : "lua";

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        BigDecimal tienVaiSingle;
        BigDecimal baseRate;

        // 1. Calculate Base Rate & Fabric Cost with Small Sheet Tiers
        if (activeSingleArea.compareTo(new BigDecimal("0.2")) < 0) {
            // Tấm nhỏ < 0.2m2 = 35.000đ khoán
            baseRate = new BigDecimal("35000");
            tienVaiSingle = new BigDecimal("35000");
            appliedRules.add("HIFLEX_SMALL_TIER_UNDER_0.2M2_35K");
        } else if (activeSingleArea.compareTo(new BigDecimal("0.3")) < 0) {
            // Tấm nhỏ < 0.3m2 = 50.000đ khoán
            baseRate = new BigDecimal("50000");
            tienVaiSingle = new BigDecimal("50000");
            appliedRules.add("HIFLEX_SMALL_TIER_UNDER_0.3M2_50K");
        } else {
            // Tấm ≥ 0.3m2: Áp dụng bảng đơn giá bậc thang (từ ảnh & quy tắc xưởng)
            BigDecimal minSide = width.min(height);
            BigDecimal maxSide = width.max(height);

            if (totalAreaAllItems.compareTo(new BigDecimal("10.0")) > 0) {
                baseRate = new BigDecimal("60000");
            } else if (totalAreaAllItems.compareTo(new BigDecimal("8.0")) > 0) {
                baseRate = new BigDecimal("70000");
            } else if (totalAreaAllItems.compareTo(new BigDecimal("5.0")) > 0) {
                baseRate = new BigDecimal("80000");
            } else if (totalAreaAllItems.compareTo(new BigDecimal("3.0")) > 0) {
                baseRate = new BigDecimal("90000");
            } else if (minSide.compareTo(BigDecimal.ONE) > 0) {
                // Hai cạnh đều > 1m
                baseRate = new BigDecimal("100000");
            } else if (maxSide.compareTo(BigDecimal.ONE) > 0 && minSide.compareTo(new BigDecimal("0.5")) > 0) {
                // Một cạnh > 1m, cạnh còn lại > 0.5m
                baseRate = new BigDecimal("110000");
            } else {
                // Tấm nhỏ (không đạt các điều kiện trên)
                baseRate = new BigDecimal("150000");
            }

            if ("xuyenden".equals(type) || "xuyen_den".equals(type)) {
                baseRate = baseRate.add(new BigDecimal("20000")); // Bạt xuyên đèn +20k/m2
            }
            tienVaiSingle = activeSingleArea.multiply(baseRate).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("HIFLEX_MATRIX_RATE_" + baseRate + "_PER_M2");
        }

        BigDecimal tienVaiTotal = tienVaiSingle.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
        lineItems.add(new LineItem("CANVAS_PRINT", "Tiền vải/bạt hiflex (" + activeSingleArea + "m² x " + quantity + " tấm)", tienVaiTotal));

        // 2. Dán Xỏ Cây (Pole Pocket / Edge Folding +5cm margin per edge)
        String pocketMode = request.polePocketMode() != null ? request.polePocketMode().toLowerCase() : "none";
        BigDecimal extraW = BigDecimal.ZERO;
        BigDecimal extraH = BigDecimal.ZERO;
        String pocketNote = null;

        if ("top_bottom".equals(pocketMode)) {
            extraH = new BigDecimal("0.10"); // +5cm trên, +5cm dưới = +10cm (0.10m)
            pocketNote = "Dán xỏ cây 2 đầu trên/dưới (+5cm/cạnh lề keo)";
        } else if ("left_right".equals(pocketMode)) {
            extraW = new BigDecimal("0.10"); // +5cm trái, +5cm phải = +10cm (0.10m)
            pocketNote = "Dán xỏ cây 2 đầu 2 bên (+5cm/cạnh lề keo)";
        } else if ("all_4".equals(pocketMode)) {
            extraW = new BigDecimal("0.10");
            extraH = new BigDecimal("0.10");
            pocketNote = "Dán xỏ cây cả 4 cạnh (+5cm/cạnh lề keo)";
        }

        if (pocketNote != null) {
            lineItems.add(new LineItem("POLE_POCKET", pocketNote, BigDecimal.ZERO));
            appliedRules.add("HIFLEX_POLE_POCKET_" + pocketMode.toUpperCase());
        }

        // Padded area calculation with margins & pole pockets
        BigDecimal marginM = request.marginCm() != null ? new BigDecimal(request.marginCm()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP) : new BigDecimal("0.05");
        BigDecimal wPad = width.add(extraW).add(marginM.multiply(new BigDecimal("2"))).setScale(4, RoundingMode.HALF_UP);
        BigDecimal hPad = height.add(extraH).add(marginM.multiply(new BigDecimal("2"))).setScale(4, RoundingMode.HALF_UP);
        BigDecimal paddedSingleArea = wPad.multiply(hPad).setScale(4, RoundingMode.HALF_UP);

        // 3. Gia Công Đóng Khoen (2k/cái) & Khuyến Mãi cho Đơn > 150k (Tặng 4 khoen + dán 2 cạnh ngắn)
        int eyeletCount = request.eyeletCount() != null ? request.eyeletCount() : 0;
        BigDecimal tienKhoen = BigDecimal.ZERO;
        boolean isPromoEligible = tienVaiTotal.compareTo(new BigDecimal("150000")) > 0;

        if (eyeletCount > 0) {
            if (isPromoEligible) {
                int chargeableEyelets = Math.max(0, eyeletCount - 4);
                tienKhoen = new BigDecimal(chargeableEyelets).multiply(new BigDecimal("2000")).multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
                lineItems.add(new LineItem("EYELETS", "Gia công đóng khoen (" + eyeletCount + " cái/tấm - Tặng 4 khoen cho đơn >150k)", tienKhoen));
                appliedRules.add("PROMO_FREE_4_EYELETS_ORDER_OVER_150K");
            } else {
                tienKhoen = new BigDecimal(eyeletCount).multiply(new BigDecimal("2000")).multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
                lineItems.add(new LineItem("EYELETS", "Gia công đóng khoen (" + eyeletCount + " cái x 2.000đ)", tienKhoen));
            }
        }

        // 4. Khung Sắt & Chân Khung
        int tubeSize = request.frameTubeSize() != null ? request.frameTubeSize() : 0;
        BigDecimal tienSat = BigDecimal.ZERO;
        BigDecimal tienChan = BigDecimal.ZERO;

        if (tubeSize > 0) {
            BigDecimal frameRatePerM = (tubeSize == 25) ? new BigDecimal("105000") : ((tubeSize == 20) ? new BigDecimal("85000") : new BigDecimal("65000"));
            BigDecimal chuViPerItem = width.add(height).multiply(new BigDecimal("2"));
            tienSat = chuViPerItem.multiply(frameRatePerM).multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            lineItems.add(new LineItem("FRAME", "Khung sắt " + tubeSize + "mm (" + chuViPerItem + "m @ " + frameRatePerM + "đ/m)", tienSat));

            if (Boolean.TRUE.equals(request.hasLeg())) {
                BigDecimal totalLegM = new BigDecimal("4.0").multiply(BigDecimal.valueOf(quantity));
                tienChan = frameRatePerM.multiply(totalLegM).setScale(0, RoundingMode.HALF_UP);
                lineItems.add(new LineItem("FRAME_LEGS", "Thêm 2 chân khung (4m @ " + frameRatePerM + "đ/m)", tienChan));
                appliedRules.add("HIFLEX_FRAME_LEGS_4M");
            }
        }

        // 5. Cán màng (50k/m2)
        BigDecimal tienCan = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.hasLamination())) {
            tienCan = paddedSingleArea.multiply(BigDecimal.valueOf(quantity)).multiply(new BigDecimal("50000")).setScale(0, RoundingMode.HALF_UP);
            lineItems.add(new LineItem("LAMINATION", "Phí cán màng (50k/m²)", tienCan));
            appliedRules.add("HIFLEX_LAMINATION");
        }

        BigDecimal totalPrice = tienVaiTotal.add(tienSat).add(tienChan).add(tienCan).add(tienKhoen).setScale(0, RoundingMode.HALF_UP);
        BigDecimal singleUnitPrice = totalPrice.divide(BigDecimal.valueOf(quantity), 0, RoundingMode.HALF_UP);

        String note = "Bạt Hiflex | " + type + " | Kích thước: " + width + "m x " + height + "m (" + activeSingleArea + "m²)";

        return new CalculatePriceResponse(
                "HIFLEX", false, activeSingleArea, totalAreaAllItems, baseRate, tienCan, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
