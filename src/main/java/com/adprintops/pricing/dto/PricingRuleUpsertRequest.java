package com.adprintops.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PricingRuleUpsertRequest(
        @NotBlank(message = "Category code is required")
        String categoryCode,

        @NotBlank(message = "Rule name is required")
        String ruleName,

        @NotNull(message = "Min area is required")
        @DecimalMin(value = "0.0", message = "Min area must be non-negative")
        BigDecimal minAreaSqm,

        BigDecimal maxAreaSqm,

        @NotNull(message = "Price per sqm is required")
        @DecimalMin(value = "0.0", message = "Price per sqm must be non-negative")
        BigDecimal pricePerSqm,

        @NotNull(message = "Lamination fee per sqm is required")
        @DecimalMin(value = "0.0", message = "Lamination fee must be non-negative")
        BigDecimal laminationFeePerSqm,

        boolean active,
        String note,
        String updatedBy
) {}
