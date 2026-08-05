package com.adprintops.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        Long customerId,
        Long createdBy,
        BigDecimal totalAmount,
        String note,
        String recipientName,
        String recipientPhone,
        String recipientAddress,
        BigDecimal paidAmount,
        String paymentStatus,
        String paymentMethod,
        List<CreateOrderItemRequest> items
) {}
