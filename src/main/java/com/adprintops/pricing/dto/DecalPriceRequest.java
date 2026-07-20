package com.adprintops.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Java Record DTO for Decal Pricing Request.
 */
public record DecalPriceRequest(
        @NotNull(message = "Chiều ngang (m) không được trống")
        @DecimalMin(value = "0.01", message = "Chiều ngang phải lớn hơn 0")
        BigDecimal widthM,

        @NotNull(message = "Chiều cao (m) không được trống")
        @DecimalMin(value = "0.01", message = "Chiều cao phải lớn hơn 0")
        BigDecimal heightM,

        @Min(value = 1, message = "Số lượng phải ít nhất là 1")
        int quantity,

        String decalType, // "thuong" (mặc định) hoặc "trong" (hệ số 1.5)

        boolean hasLamination // true: Cán màng (+50k/m2)
) {}
