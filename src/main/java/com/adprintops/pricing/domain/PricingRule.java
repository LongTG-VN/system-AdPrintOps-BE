package com.adprintops.pricing.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "pricing_rules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pricing_rules_category_min_area",
                columnNames = {"category_code", "min_area_sqm"}
        ),
        indexes = @Index(
                name = "idx_pricing_rules_category_active_min_area",
                columnList = "category_code, is_active, min_area_sqm"
        )
)
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "min_area_sqm", nullable = false, precision = 8, scale = 3)
    private BigDecimal minAreaSqm;

    @Column(name = "max_area_sqm", precision = 8, scale = 3)
    private BigDecimal maxAreaSqm;

    @Column(name = "price_per_sqm", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerSqm;

    @Column(name = "lamination_fee_per_sqm", nullable = false, precision = 12, scale = 2)
    private BigDecimal laminationFeePerSqm;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "note")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PricingRule() {}

    public PricingRule(String categoryCode, String ruleName, BigDecimal minAreaSqm, BigDecimal maxAreaSqm, BigDecimal pricePerSqm, BigDecimal laminationFeePerSqm, boolean active, String note) {
        this.categoryCode = categoryCode;
        this.ruleName = ruleName;
        this.minAreaSqm = minAreaSqm;
        this.maxAreaSqm = maxAreaSqm;
        this.pricePerSqm = pricePerSqm;
        this.laminationFeePerSqm = laminationFeePerSqm;
        this.active = active;
        this.note = note;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public BigDecimal getMinAreaSqm() { return minAreaSqm; }
    public void setMinAreaSqm(BigDecimal minAreaSqm) { this.minAreaSqm = minAreaSqm; }

    public BigDecimal getMaxAreaSqm() { return maxAreaSqm; }
    public void setMaxAreaSqm(BigDecimal maxAreaSqm) { this.maxAreaSqm = maxAreaSqm; }

    public BigDecimal getPricePerSqm() { return pricePerSqm; }
    public void setPricePerSqm(BigDecimal pricePerSqm) { this.pricePerSqm = pricePerSqm; }

    public BigDecimal getLaminationFeePerSqm() { return laminationFeePerSqm; }
    public void setLaminationFeePerSqm(BigDecimal laminationFeePerSqm) { this.laminationFeePerSqm = laminationFeePerSqm; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
