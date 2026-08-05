package com.adprintops.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderCode,
        Long customerId,
        Long createdBy,
        BigDecimal totalAmount,
        String status,
        String note,
        String recipientName,
        String recipientPhone,
        String recipientAddress,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        String paymentStatus,
        String paymentMethod,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {}
