package com.adprintops.pricing.dto;

import java.math.BigDecimal;

public record LineItem(
        String code,
        String label,
        BigDecimal amount
) {}
