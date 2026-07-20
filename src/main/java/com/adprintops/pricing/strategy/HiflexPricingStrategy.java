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

        BigDecimal marginM = request.marginCm() != null ? new BigDecimal(request.marginCm()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP) : new BigDecimal("0.05");

        BigDecimal wPad = width.add(marginM.multiply(new BigDecimal("2"))).setScale(4, RoundingMode.HALF_UP);
        BigDecimal hPad = height.add(marginM.multiply(new BigDecimal("2"))).setScale(4, RoundingMode.HALF_UP);

        BigDecimal activeSingleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);
        BigDecimal paddedSingleArea = wPad.multiply(hPad).setScale(4, RoundingMode.HALF_UP);
        BigDecimal marginSingleArea = paddedSingleArea.subtract(activeSingleArea).max(BigDecimal.ZERO);

        String type = request.hiflexType() != null ? request.hiflexType().toLowerCase() : "lua";

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        BigDecimal tienVai;
        BigDecimal baseRate;

        if ("xuyenden".equals(type) || "xuyen_den".equals(type)) {
            BigDecimal activePrintArea = activeSingleArea.multiply(BigDecimal.valueOf(quantity));
            BigDecimal paddedArea = paddedSingleArea.multiply(BigDecimal.valueOf(quantity));
            BigDecimal marginArea = marginSingleArea.multiply(BigDecimal.valueOf(quantity));

            // Roll optimization for xuyen den (1.4m & 1.8m)
            BigDecimal[] rolls = new BigDecimal[]{new BigDecimal("1.4"), new BigDecimal("1.8")};
            BigDecimal minRollArea = new BigDecimal("999999");

            for (BigDecimal r : rolls) {
                if (wPad.compareTo(r) <= 0) {
                    BigDecimal k = r.divide(wPad, 0, RoundingMode.FLOOR);
                    if (k.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal rows = BigDecimal.valueOf(quantity).divide(k, 0, RoundingMode.CEILING);
                        BigDecimal length = rows.multiply(hPad);
                        BigDecimal area = r.multiply(length);
                        if (area.compareTo(minRollArea) < 0) minRollArea = area;
                    }
                }
                if (hPad.compareTo(r) <= 0) {
                    BigDecimal k = r.divide(hPad, 0, RoundingMode.FLOOR);
                    if (k.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal rows = BigDecimal.valueOf(quantity).divide(k, 0, RoundingMode.CEILING);
                        BigDecimal length = rows.multiply(wPad);
                        BigDecimal area = r.multiply(length);
                        if (area.compareTo(minRollArea) < 0) minRollArea = area;
                    }
                }
            }

            BigDecimal rollArea = minRollArea.compareTo(new BigDecimal("999999")) < 0 ? minRollArea : paddedArea;
            BigDecimal leftoverArea = rollArea.subtract(paddedArea).max(BigDecimal.ZERO);

            tienVai = activePrintArea.multiply(new BigDecimal("100000"))
                    .add(marginArea.multiply(new BigDecimal("35000")))
                    .add(leftoverArea.multiply(new BigDecimal("35000")));
            baseRate = new BigDecimal("100000");
            appliedRules.add("HIFLEX_XUYEN_DEN_ROLL_PACKING");

        } else {
            if ("lua".equals(type)) {
                if (width.compareTo(BigDecimal.ONE) < 0 && height.compareTo(BigDecimal.ONE) < 0) baseRate = new BigDecimal("150000");
                else if (width.compareTo(BigDecimal.ONE) >= 0 && height.compareTo(BigDecimal.ONE) >= 0) baseRate = new BigDecimal("80000");
                else baseRate = new BigDecimal("110000");
            } else {
                // decal / decal_dan
                if (width.compareTo(BigDecimal.ONE) < 0 && height.compareTo(BigDecimal.ONE) < 0) baseRate = new BigDecimal("200000");
                else if (width.compareTo(BigDecimal.ONE) >= 0 && height.compareTo(BigDecimal.ONE) >= 0) baseRate = new BigDecimal("120000");
                else baseRate = new BigDecimal("150000");
            }

            BigDecimal activePrintArea = activeSingleArea.multiply(BigDecimal.valueOf(quantity));
            BigDecimal marginArea = marginSingleArea.multiply(BigDecimal.valueOf(quantity));

            tienVai = activePrintArea.multiply(baseRate).add(marginArea.multiply(new BigDecimal("35000")));
            appliedRules.add("HIFLEX_" + type.toUpperCase());
        }

        lineItems.add(new LineItem("CANVAS_PRINT", "Tiền vải/bạt (" + type + ")", tienVai));

        // Frame calculation: v16 (65k/m), v20 (85k/m), v25 (105k/m)
        int tubeSize = request.frameTubeSize() != null ? request.frameTubeSize() : 0;
        BigDecimal tienSat = BigDecimal.ZERO;
        BigDecimal tienChan = BigDecimal.ZERO;

        if (tubeSize > 0) {
            BigDecimal frameRatePerM = (tubeSize == 25) ? new BigDecimal("105000") : ((tubeSize == 20) ? new BigDecimal("85000") : new BigDecimal("65000"));
            BigDecimal chuViPerItem = width.add(height).multiply(new BigDecimal("2"));
            tienSat = chuViPerItem.multiply(frameRatePerM).multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            lineItems.add(new LineItem("FRAME", "Khung sắt " + tubeSize + "mm (" + chuViPerItem + "m @ " + frameRatePerM + "đ/m)", tienSat));

            if (Boolean.TRUE.equals(request.hasLeg())) {
                // Legs: 2 legs x 2m = 4m per item exactly!
                BigDecimal totalLegM = new BigDecimal("4.0").multiply(BigDecimal.valueOf(quantity));
                tienChan = frameRatePerM.multiply(totalLegM).setScale(0, RoundingMode.HALF_UP);
                lineItems.add(new LineItem("FRAME_LEGS", "Thêm 2 chân khung (4m @ " + frameRatePerM + "đ/m)", tienChan));
                appliedRules.add("HIFLEX_FRAME_LEGS_4M");
            }
        }

        BigDecimal tienCan = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.hasLamination())) {
            tienCan = paddedSingleArea.multiply(BigDecimal.valueOf(quantity)).multiply(new BigDecimal("50000")).setScale(0, RoundingMode.HALF_UP);
            lineItems.add(new LineItem("LAMINATION", "Phí cán màng (50k/m²)", tienCan));
            appliedRules.add("HIFLEX_LAMINATION");
        }

        BigDecimal totalPrice = tienVai.add(tienSat).add(tienChan).add(tienCan).setScale(0, RoundingMode.HALF_UP);
        BigDecimal singleUnitPrice = totalPrice.divide(BigDecimal.valueOf(quantity), 0, RoundingMode.HALF_UP);
        BigDecimal totalArea = activeSingleArea.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_UP);

        String note = "Bảng Bạt Hiflex | " + type + " | Kích thước: " + width + "m x " + height + "m";

        return new CalculatePriceResponse(
                "HIFLEX", false, activeSingleArea, totalArea, baseRate, tienCan, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
