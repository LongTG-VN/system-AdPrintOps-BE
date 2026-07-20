package com.adprintops.pricing.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "pricing_configurations")
@Getter
@Setter
public class PricingConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_name", nullable = false)
    private String configName;

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal multiplier = BigDecimal.ONE;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "note")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    public PricingConfiguration() {
    }

    public PricingConfiguration(String categoryCode, String configKey, String configName, BigDecimal basePrice, BigDecimal multiplier, boolean active, String note) {
        this.categoryCode = categoryCode;
        this.configKey = configKey;
        this.configName = configName;
        this.basePrice = basePrice;
        this.multiplier = multiplier;
        this.active = active;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getConfigName() {
        return configName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public boolean isActive() {
        return active;
    }

    public String getNote() {
        return note;
    }
}
