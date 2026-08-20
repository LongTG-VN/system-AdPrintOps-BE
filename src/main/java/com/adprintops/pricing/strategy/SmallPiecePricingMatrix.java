package com.adprintops.pricing.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class SmallPiecePricingMatrix {

    private record SizeKey(int minCm, int maxCm) {}
    private record PiecePrice(int price1, int price2) {}

    private static final Map<SizeKey, PiecePrice> MATRIX = new HashMap<>();

    static {
        // Shared small piece price lookup matrix for Decal & Hiflex/Lụa from xưởng price tables:
        add(10, 15, 15000, 25000);
        add(10, 20, 15000, 25000);
        add(10, 30, 18000, 30000);
        add(15, 20, 18000, 30000);
        add(15, 30, 20000, 35000);
        add(15, 40, 22000, 40000);
        add(20, 20, 20000, 35000);
        add(20, 30, 25000, 40000);
        add(20, 40, 28000, 50000);
        add(20, 50, 30000, 55000);
        add(20, 60, 35000, 60000);
        add(20, 80, 40000, 70000);
        add(20, 100, 50000, 90000);
        add(30, 30, 25000, 45000);
        add(30, 40, 30000, 50000);
        add(30, 50, 35000, 60000);
        add(30, 60, 40000, 70000);
        add(30, 80, 45000, 80000);
        add(30, 100, 55000, 100000);
        add(40, 40, 30000, 55000);
        add(40, 50, 35000, 65000);
        add(40, 60, 40000, 70000);
        add(40, 80, 50000, 90000);
        add(40, 100, 60000, 110000);
        add(50, 50, 40000, 70000);
        add(50, 60, 45000, 80000);
        add(50, 70, 50000, 90000);
        add(50, 80, 55000, 100000);
        add(50, 100, 65000, 120000);
        add(60, 60, 50000, 90000);
        add(60, 80, 60000, 110000);
        add(60, 90, 70000, 130000);
    }

    private static void add(int w, int h, int p1, int p2) {
        int min = Math.min(w, h);
        int max = Math.max(w, h);
        MATRIX.put(new SizeKey(min, max), new PiecePrice(p1, p2));
    }

    public static BigDecimal findPrice(BigDecimal widthM, BigDecimal heightM, int quantity) {
        if (widthM == null || heightM == null || quantity <= 0) return null;
        int wCm = (int) Math.round(widthM.doubleValue() * 100);
        int hCm = (int) Math.round(heightM.doubleValue() * 100);
        int min = Math.min(wCm, hCm);
        int max = Math.max(wCm, hCm);

        // Only apply matrix if item size is small (<= 60x90cm / <= 0.54m²)
        if (max > 100 || (min > 60 && max > 90)) {
            return null;
        }

        PiecePrice price = MATRIX.get(new SizeKey(min, max));
        if (price == null) {
            // Find closest match where dimensions fit within min & max
            for (Map.Entry<SizeKey, PiecePrice> entry : MATRIX.entrySet()) {
                if (min <= entry.getKey().minCm() && max <= entry.getKey().maxCm()) {
                    if (price == null || entry.getValue().price1() < price.price1()) {
                        price = entry.getValue();
                    }
                }
            }
        }

        if (price == null) return null;

        if (quantity == 1) {
            return new BigDecimal(price.price1());
        } else if (quantity == 2) {
            return new BigDecimal(price.price2());
        } else {
            BigDecimal base2 = new BigDecimal(price.price2());
            BigDecimal perSheetRate = base2.divide(new BigDecimal("2"), 0, RoundingMode.HALF_UP);
            BigDecimal extraQty = new BigDecimal(quantity - 2);
            return base2.add(perSheetRate.multiply(extraQty));
        }
    }
}
