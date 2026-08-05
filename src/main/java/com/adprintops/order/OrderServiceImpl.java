package com.adprintops.order;

import com.adprintops.design.DesignService;
import com.adprintops.design.domain.DesignTaskRepository;
import com.adprintops.order.domain.Order;
import com.adprintops.order.domain.OrderItem;
import com.adprintops.order.domain.OrderRepository;
import com.adprintops.order.dto.CreateOrderItemRequest;
import com.adprintops.order.dto.CreateOrderRequest;
import com.adprintops.order.dto.OrderItemResponse;
import com.adprintops.order.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DesignService designService;
    private final DesignTaskRepository designTaskRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            DesignService designService,
                            DesignTaskRepository designTaskRepository) {
        this.orderRepository = orderRepository;
        this.designService = designService;
        this.designTaskRepository = designTaskRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrderFromPricing(CreateOrderRequest request) {
        String orderCode = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        BigDecimal total = request.totalAmount() != null ? request.totalAmount() : BigDecimal.ZERO;
        BigDecimal paid = request.paidAmount() != null ? request.paidAmount() : BigDecimal.ZERO;
        String payMethod = request.paymentMethod() != null && !request.paymentMethod().isBlank() ? request.paymentMethod() : "CASH";

        String payStatus = request.paymentStatus();
        if (payStatus == null || payStatus.isBlank()) {
            if (paid.compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0) {
                payStatus = "PAID";
            } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
                payStatus = "PARTIALLY_PAID";
            } else {
                payStatus = "UNPAID";
            }
        }

        // Nếu đã cọc hoặc thanh toán đủ -> Chuyển sang DESIGNING (Thiết kế). Chưa thanh toán -> PENDING (Chờ xác nhận)
        String initialOrderStatus = ("PAID".equals(payStatus) || "PARTIALLY_PAID".equals(payStatus))
                ? "DESIGNING"
                : "PENDING";

        Order order = Order.builder()
                .orderCode(orderCode)
                .customerId(request.customerId())
                .createdBy(request.createdBy())
                .totalAmount(total)
                .status(initialOrderStatus)
                .note(request.note())
                .recipientName(request.recipientName())
                .recipientPhone(request.recipientPhone())
                .recipientAddress(request.recipientAddress())
                .paidAmount(paid)
                .paymentStatus(payStatus)
                .paymentMethod(payMethod)
                .items(new ArrayList<>())
                .build();

        if (request.items() != null) {
            for (CreateOrderItemRequest itemReq : request.items()) {
                OrderItem item = OrderItem.builder()
                        .order(order)
                        .categoryCode(itemReq.categoryCode())
                        .productName(itemReq.productName() != null ? itemReq.productName() : itemReq.categoryCode())
                        .widthCm(itemReq.widthCm())
                        .heightCm(itemReq.heightCm())
                        .quantity(itemReq.quantity() != null ? itemReq.quantity() : 1)
                        .materialCode(itemReq.materialCode())
                        .calculatedPrice(itemReq.calculatedPrice() != null ? itemReq.calculatedPrice() : BigDecimal.ZERO)
                        .specificationsJson(itemReq.specificationsJson())
                        .build();
                order.getItems().add(item);
            }
        }

        Order savedOrder = orderRepository.save(order);

        // Tự động sinh Task Thiết kế cho từng OrderItem vừa tạo
        for (OrderItem item : savedOrder.getItems()) {
            designService.createDesignTaskForOrderItem(item.getId(), item.getCategoryCode(), item.getProductName());
        }

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + id));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với Mã: " + orderCode));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, String status, String note, BigDecimal totalAmount, String recipientName, String recipientPhone, String recipientAddress, BigDecimal paidAmount, String paymentStatus, String paymentMethod, List<CreateOrderItemRequest> items) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + id));

        if (status != null && !status.isBlank()) {
            order.setStatus(status);
        }
        if (note != null) {
            order.setNote(note);
        }
        if (totalAmount != null) {
            order.setTotalAmount(totalAmount);
        }
        if (recipientName != null) {
            order.setRecipientName(recipientName);
        }
        if (recipientPhone != null) {
            order.setRecipientPhone(recipientPhone);
        }
        if (recipientAddress != null) {
            order.setRecipientAddress(recipientAddress);
        }
        if (paidAmount != null) {
            order.setPaidAmount(paidAmount);
        }
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            order.setPaymentMethod(paymentMethod);
        }

        if (paymentStatus != null && !paymentStatus.isBlank()) {
            order.setPaymentStatus(paymentStatus);
            if (("PAID".equals(paymentStatus) || "PARTIALLY_PAID".equals(paymentStatus)) && ("PENDING".equals(order.getStatus()) || order.getStatus() == null)) {
                order.setStatus("DESIGNING");
            }
        } else if (paidAmount != null || totalAmount != null) {
            BigDecimal currentTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal currentPaid = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
            if (currentPaid.compareTo(currentTotal) >= 0 && currentTotal.compareTo(BigDecimal.ZERO) > 0) {
                order.setPaymentStatus("PAID");
                if ("PENDING".equals(order.getStatus()) || order.getStatus() == null) {
                    order.setStatus("DESIGNING");
                }
            } else if (currentPaid.compareTo(BigDecimal.ZERO) > 0) {
                order.setPaymentStatus("PARTIALLY_PAID");
                if ("PENDING".equals(order.getStatus()) || order.getStatus() == null) {
                    order.setStatus("DESIGNING");
                }
            } else {
                order.setPaymentStatus("UNPAID");
            }
        }

        // Cập nhật thông số từng item nếu có
        if (items != null && !items.isEmpty() && order.getItems() != null) {
            for (int i = 0; i < Math.min(items.size(), order.getItems().size()); i++) {
                CreateOrderItemRequest itemReq = items.get(i);
                OrderItem existingItem = order.getItems().get(i);

                if (itemReq.widthCm() != null && itemReq.widthCm().compareTo(BigDecimal.ZERO) > 0) {
                    existingItem.setWidthCm(itemReq.widthCm());
                }
                if (itemReq.heightCm() != null && itemReq.heightCm().compareTo(BigDecimal.ZERO) > 0) {
                    existingItem.setHeightCm(itemReq.heightCm());
                }
                if (itemReq.quantity() != null && itemReq.quantity() > 0) {
                    existingItem.setQuantity(itemReq.quantity());
                }
                if (itemReq.calculatedPrice() != null) {
                    existingItem.setCalculatedPrice(itemReq.calculatedPrice());
                }
            }
        }

        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + id));

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                designTaskRepository.findByOrderItemId(item.getId())
                        .ifPresent(designTaskRepository::delete);
            }
        }

        orderRepository.delete(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItem> itemsList = order.getItems() != null ? order.getItems() : java.util.Collections.emptyList();
        List<OrderItemResponse> itemResponses = itemsList.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        order.getId(),
                        item.getCategoryCode(),
                        item.getProductName(),
                        item.getWidthCm(),
                        item.getHeightCm(),
                        item.getQuantity(),
                        item.getMaterialCode(),
                        item.getCalculatedPrice(),
                        item.getSpecificationsJson(),
                        item.getCreatedAt()
                )).toList();

        BigDecimal paid = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal remaining = total.subtract(paid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerId(),
                order.getCreatedBy(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getNote(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getRecipientAddress(),
                paid,
                remaining,
                order.getPaymentStatus() != null ? order.getPaymentStatus() : "UNPAID",
                order.getPaymentMethod() != null ? order.getPaymentMethod() : "CASH",
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
