package com.adprintops.pricing.dto;

import java.math.BigDecimal;
import java.util.List;

public record CalculatePriceResponse(
        String categoryCode,
        boolean vatIncluded,
        BigDecimal singleAreaSqm,
        BigDecimal totalAreaSqm,
        BigDecimal ratePerSqm,
        BigDecimal laminationCost,
        BigDecimal singleUnitPrice,
        BigDecimal totalPrice,
        String currency,
        List<LineItem> lineItems,
        List<String> appliedRules,
        String breakdownNote
) {
    public CalculatePriceResponse(
            String categoryCode,
            BigDecimal singleAreaSqm,
            BigDecimal totalAreaSqm,
            BigDecimal ratePerSqm,
            BigDecimal laminationCost,
            BigDecimal singleUnitPrice,
            BigDecimal totalPrice,
            String currency,
            List<LineItem> lineItems,
            List<String> appliedRules,
            String breakdownNote
    ) {
        this(categoryCode, false, singleAreaSqm, totalAreaSqm, ratePerSqm, laminationCost, singleUnitPrice, totalPrice, currency, lineItems, appliedRules, breakdownNote);
    }
}
