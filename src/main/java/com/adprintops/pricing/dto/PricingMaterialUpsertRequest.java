package com.adprintops.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PricingMaterialUpsertRequest(
        @NotBlank(message = "Category code is required")
        String categoryCode,

        @NotBlank(message = "Material code is required")
        String materialCode,

        @NotBlank(message = "Material name is required")
        String materialName,

        @NotNull(message = "Multiplier is required")
        @DecimalMin(value = "0.01", message = "Multiplier must be greater than 0")
        BigDecimal multiplier,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", message = "Base price must be non-negative")
        BigDecimal basePrice,

        boolean active,
        String updatedBy
) {}
