package com.adprintops.pricing.dto;

import java.math.BigDecimal;

/**
 * Java Record DTO for Decal Pricing Result Response.
 */
public record DecalPriceResponse(
        BigDecimal singleAreaSqm,
        BigDecimal totalAreaSqm,
        BigDecimal ratePerSqm,
        BigDecimal laminationCost,
        BigDecimal singleUnitPrice,
        BigDecimal totalPrice,
        String category,
        String pricingNote
) {}
