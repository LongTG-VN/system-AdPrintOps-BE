package com.adprintops.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CalculatePriceRequest(
        @NotBlank(message = "Category code is required")
        String categoryCode,

        BigDecimal widthM,
        BigDecimal heightM,
        Integer quantity,
        String materialCode,
        Boolean hasLamination,
        Boolean hasDieCut,
        Integer boxCount,
        Integer frameTubeSize, // 16, 20, 25mm
        Integer paperGsm,
        Integer sheetCount,
        BigDecimal customFee,

        // Remediation Category-Specific Inputs
        String cutMode, // chuan, vien, le
        BigDecimal maxSideM,
        Integer rollWidthTac, // 6, 10
        String hiflexType, // lua, xuyen_den, decal_dan
        Integer marginCm, // 0, 5, 10
        Boolean hasLeg,
        String paperSubtype, // in_le, ep_nhua, to_roi_a4, to_roi_a5, bang_ten
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
}
