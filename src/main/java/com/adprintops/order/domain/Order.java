package com.adprintops.order.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "CONFIRMED";

    @Column(name = "note")
    private String note;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "recipient_address")
    private String recipientAddress;

    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "payment_status", length = 30)
    private String paymentStatus = "UNPAID";

    @Column(name = "payment_method", length = 30)
    private String paymentMethod = "CASH";

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Order() {
    }

    public Order(Long id, String orderCode, Long customerId, Long createdBy, BigDecimal totalAmount, String status, String note, String recipientName, String recipientPhone, String recipientAddress, BigDecimal paidAmount, String paymentStatus, String paymentMethod, List<OrderItem> items) {
        this.id = id;
        this.orderCode = orderCode;
        this.customerId = customerId;
        this.createdBy = createdBy;
        this.totalAmount = totalAmount;
        this.status = status;
        this.note = note;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.recipientAddress = recipientAddress;
        this.paidAmount = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        this.paymentStatus = paymentStatus != null ? paymentStatus : "UNPAID";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
        if (items != null) {
            this.items = items;
        }
    }

    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class OrderBuilder {
        private Long id;
        private String orderCode;
        private Long customerId;
        private Long createdBy;
        private BigDecimal totalAmount;
        private String status;
        private String note;
        private String recipientName;
        private String recipientPhone;
        private String recipientAddress;
        private BigDecimal paidAmount = BigDecimal.ZERO;
        private String paymentStatus = "UNPAID";
        private String paymentMethod = "CASH";
        private List<OrderItem> items = new ArrayList<>();

        public OrderBuilder id(Long id) { this.id = id; return this; }
        public OrderBuilder orderCode(String orderCode) { this.orderCode = orderCode; return this; }
        public OrderBuilder customerId(Long customerId) { this.customerId = customerId; return this; }
        public OrderBuilder createdBy(Long createdBy) { this.createdBy = createdBy; return this; }
        public OrderBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public OrderBuilder status(String status) { this.status = status; return this; }
        public OrderBuilder note(String note) { this.note = note; return this; }
        public OrderBuilder recipientName(String recipientName) { this.recipientName = recipientName; return this; }
        public OrderBuilder recipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; return this; }
        public OrderBuilder recipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; return this; }
        public OrderBuilder paidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; return this; }
        public OrderBuilder paymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; return this; }
        public OrderBuilder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
        public OrderBuilder items(List<OrderItem> items) { this.items = items; return this; }

        public Order build() {
            return new Order(id, orderCode, customerId, createdBy, totalAmount, status, note, recipientName, recipientPhone, recipientAddress, paidAmount, paymentStatus, paymentMethod, items);
        }
    }
}
