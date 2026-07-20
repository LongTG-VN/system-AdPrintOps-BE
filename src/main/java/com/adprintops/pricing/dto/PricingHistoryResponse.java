package com.adprintops.pricing.dto;

import java.time.Instant;

public record PricingHistoryResponse(
        Long id,
        String targetType,
        Long targetId,
        String fieldName,
        String oldValue,
        String newValue,
        String changedBy,
        Instant createdAt
) {}
