package com.adprintops.order;

import com.adprintops.order.dto.CreateOrderItemRequest;
import com.adprintops.order.dto.CreateOrderRequest;
import com.adprintops.order.dto.OrderResponse;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    OrderResponse createOrderFromPricing(CreateOrderRequest request);
    OrderResponse getOrderById(Long id);
    OrderResponse getOrderByCode(String orderCode);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrder(Long id, String status, String note, BigDecimal totalAmount, String recipientName, String recipientPhone, String recipientAddress, BigDecimal paidAmount, String paymentStatus, String paymentMethod, List<CreateOrderItemRequest> items);
    void deleteOrder(Long id);
}
