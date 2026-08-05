package com.adprintops.order.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "width_cm", precision = 10, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", precision = 10, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "material_code", length = 50)
    private String materialCode;

    @Column(name = "calculated_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal calculatedPrice = BigDecimal.ZERO;

    @Column(name = "specifications_json", columnDefinition = "TEXT")
    private String specificationsJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public OrderItem() {
    }

    public OrderItem(Long id, Order order, String categoryCode, String productName, BigDecimal widthCm, BigDecimal heightCm, Integer quantity, String materialCode, BigDecimal calculatedPrice, String specificationsJson) {
        this.id = id;
        this.order = order;
        this.categoryCode = categoryCode;
        this.productName = productName;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.quantity = quantity;
        this.materialCode = materialCode;
        this.calculatedPrice = calculatedPrice;
        this.specificationsJson = specificationsJson;
    }

    public static OrderItemBuilder builder() {
        return new OrderItemBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getWidthCm() { return widthCm; }
    public void setWidthCm(BigDecimal widthCm) { this.widthCm = widthCm; }

    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public BigDecimal getCalculatedPrice() { return calculatedPrice; }
    public void setCalculatedPrice(BigDecimal calculatedPrice) { this.calculatedPrice = calculatedPrice; }

    public String getSpecificationsJson() { return specificationsJson; }
    public void setSpecificationsJson(String specificationsJson) { this.specificationsJson = specificationsJson; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class OrderItemBuilder {
        private Long id;
        private Order order;
        private String categoryCode;
        private String productName;
        private BigDecimal widthCm;
        private BigDecimal heightCm;
        private Integer quantity;
        private String materialCode;
        private BigDecimal calculatedPrice;
        private String specificationsJson;

        public OrderItemBuilder id(Long id) { this.id = id; return this; }
        public OrderItemBuilder order(Order order) { this.order = order; return this; }
        public OrderItemBuilder categoryCode(String categoryCode) { this.categoryCode = categoryCode; return this; }
        public OrderItemBuilder productName(String productName) { this.productName = productName; return this; }
        public OrderItemBuilder widthCm(BigDecimal widthCm) { this.widthCm = widthCm; return this; }
        public OrderItemBuilder heightCm(BigDecimal heightCm) { this.heightCm = heightCm; return this; }
        public OrderItemBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderItemBuilder materialCode(String materialCode) { this.materialCode = materialCode; return this; }
        public OrderItemBuilder calculatedPrice(BigDecimal calculatedPrice) { this.calculatedPrice = calculatedPrice; return this; }
        public OrderItemBuilder specificationsJson(String specificationsJson) { this.specificationsJson = specificationsJson; return this; }

        public OrderItem build() {
            return new OrderItem(id, order, categoryCode, productName, widthCm, heightCm, quantity, materialCode, calculatedPrice, specificationsJson);
        }
    }
}
