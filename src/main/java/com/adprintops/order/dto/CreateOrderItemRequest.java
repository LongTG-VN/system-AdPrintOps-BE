package com.adprintops.order.dto;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        String categoryCode,
        String productName,
        BigDecimal widthCm,
        BigDecimal heightCm,
        Integer quantity,
        String materialCode,
        BigDecimal calculatedPrice,
        String specificationsJson
) {}
