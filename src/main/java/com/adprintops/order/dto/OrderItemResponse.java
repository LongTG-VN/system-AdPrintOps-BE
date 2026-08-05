package com.adprintops.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderItemResponse(
        Long id,
        Long orderId,
        String categoryCode,
        String productName,
        BigDecimal widthCm,
        BigDecimal heightCm,
        Integer quantity,
        String materialCode,
        BigDecimal calculatedPrice,
        String specificationsJson,
        Instant createdAt
) {}
