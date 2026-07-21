package com.adprintops.pricing.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CalculatePriceRequest(
        @NotBlank(message = "Category code is required")
        String categoryCode,

        @DecimalMin(value = "0.01", message = "Width must be greater than 0")
        BigDecimal widthM,

        @DecimalMin(value = "0.01", message = "Height must be greater than 0")
        BigDecimal heightM,

        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,
        String materialCode,
        Boolean hasLamination,
        Boolean hasDieCut,

        @Min(value = 1, message = "Box count must be at least 1")
        Integer boxCount,

        @Min(value = 1, message = "Frame tube size must be positive")
        Integer frameTubeSize, // 16, 20, 25mm

        @Min(value = 1, message = "Paper GSM must be positive")
        Integer paperGsm,

        @Min(value = 1, message = "Sheet count must be at least 1")
        Integer sheetCount,

        @DecimalMin(value = "0.0", message = "Custom fee must be non-negative")
        BigDecimal customFee,

        // Remediation Category-Specific Inputs
        String cutMode, // chuan, vien, le

        @DecimalMin(value = "0.01", message = "Maximum side must be greater than 0")
        BigDecimal maxSideM,

        @Min(value = 1, message = "Roll width must be positive")
        Integer rollWidthTac, // 6, 10
        String hiflexType, // lua, xuyen_den, decal_dan

        @Min(value = 0, message = "Margin must be non-negative")
        Integer marginCm, // 0, 5, 10
        Boolean hasLeg,
        String paperSubtype, // in_le, ep_nhua, to_roi_a4, to_roi_a5, bang_ten

        @Min(value = 1, message = "Paper sides must be at least 1")
        Integer paperSides, // 1, 2
        String tranhType, // tranh_dien, so_nha
        String tranhPreset,
        String tranhPackage
) {
    public CalculatePriceRequest(
            String categoryCode,
            BigDecimal widthM,
            BigDecimal heightM,
            Integer quantity,
            String materialCode,
            Boolean hasLamination,
            Boolean hasDieCut,
            Integer boxCount,
            Integer frameTubeSize,
            Integer paperGsm,
            Integer sheetCount,
            BigDecimal customFee
    ) {
        this(categoryCode, widthM, heightM, quantity, materialCode, hasLamination, hasDieCut, boxCount, frameTubeSize, paperGsm, sheetCount, customFee,
                null, null, null, null, null, null, null, null, null, null, null);
    }

    @JsonIgnore
    @AssertTrue(message = "Missing required pricing inputs for the selected category")
    public boolean isCategoryInputValid() {
        if (categoryCode == null || categoryCode.isBlank()) {
            return true;
        }

        boolean hasQuantity = quantity != null && quantity > 0;
        boolean hasDimensions = widthM != null && heightM != null;

        return switch (categoryCode.toUpperCase()) {
            case "DECAL", "TEM", "CAT", "BANG", "HIFLEX" -> hasDimensions && hasQuantity;
            case "CARD" -> hasQuantity || (boxCount != null && boxCount > 0);
            case "GIAY", "TRANH", "KHAC" -> hasQuantity;
            default -> true;
        };
    }
}
