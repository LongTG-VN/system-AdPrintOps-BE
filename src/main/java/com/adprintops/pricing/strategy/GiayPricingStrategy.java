package com.adprintops.pricing.strategy;

import com.adprintops.pricing.PricingConfigurationException;
import com.adprintops.pricing.domain.PricingConfiguration;
import com.adprintops.pricing.domain.PricingConfigurationRepository;
import com.adprintops.pricing.dto.CalculatePriceRequest;
import com.adprintops.pricing.dto.CalculatePriceResponse;
import com.adprintops.pricing.dto.LineItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GiayPricingStrategy implements PricingStrategy {

    private final PricingConfigurationRepository pricingConfigurationRepository;

    public GiayPricingStrategy(PricingConfigurationRepository pricingConfigurationRepository) {
        this.pricingConfigurationRepository = pricingConfigurationRepository;
    }

    @Override
    public String getCategoryCode() {
        return "GIAY";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        String rawSubtype = request.paperSubtype() != null ? request.paperSubtype().toLowerCase() : "giay";
        int quantity = request.quantity() != null && request.quantity() > 0 ? request.quantity() : (request.sheetCount() != null ? request.sheetCount() : 1);

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        BigDecimal totalPrice;
        BigDecimal singleUnitPrice;
        String configKey;
        String note;

        // Map frontend values to backend subtypes seamlessly
        if ("giay".equals(rawSubtype) || "in_le".equals(rawSubtype) || "giay_le".equals(rawSubtype)) {
            // In giấy lẻ (khổ A5/A4/A3, loại day1/day2/bong1/bong2)
            String kho = request.tranhPreset() != null ? request.tranhPreset().toUpperCase() : "A4";
            String mat = request.materialCode() != null ? request.materialCode().toUpperCase() : "DAY1";
            configKey = "GIAY_" + kho + "_" + mat;

            Optional<PricingConfiguration> configOpt = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("GIAY", configKey);

            if (configOpt.isPresent()) {
                singleUnitPrice = configOpt.get().getBasePrice().setScale(0, RoundingMode.HALF_UP);
            } else {
                // Fallback for custom dimensions sqm @ 80k/m²
                BigDecimal width = request.widthM() != null ? request.widthM() : new BigDecimal("0.21");
                BigDecimal height = request.heightM() != null ? request.heightM() : new BigDecimal("0.297");
                BigDecimal singleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);
                singleUnitPrice = singleArea.multiply(new BigDecimal("80000")).setScale(0, RoundingMode.HALF_UP);
                configKey = "SQM_80K";
            }

            totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("GIAY_" + configKey);
            lineItems.add(new LineItem("PRINT_PAPER", "In Giấy lẻ (" + quantity + " tờ)", totalPrice));
            note = "In Giấy Lẻ | " + quantity + " tờ";

        } else if ("ep".equals(rawSubtype) || "ep_nhua".equals(rawSubtype)) {
            // Ép nhựa (khổ A5/A4/A3, số mặt 1/2)
            String kho = request.tranhPreset() != null ? request.tranhPreset().toUpperCase() : "A4";
            int sides = request.paperSides() != null && request.paperSides() == 2 ? 2 : 1;
            configKey = "EP_" + kho + "_" + sides;

            Optional<PricingConfiguration> configOpt = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("GIAY", configKey);

            if (configOpt.isEmpty()) {
                // Default fallback if specific A3/A4/A5 preset is omitted
                configOpt = pricingConfigurationRepository.findByCategoryCodeAndConfigKeyAndActiveTrue("GIAY", "EP_A4_1");
            }

            singleUnitPrice = configOpt.map(PricingConfiguration::getBasePrice).orElse(new BigDecimal("15000")).setScale(0, RoundingMode.HALF_UP);
            totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("GIAY_" + configKey);
            lineItems.add(new LineItem("EP_NHUA", "Ép nhựa dẻo (" + quantity + " tờ)", totalPrice));
            note = "Ép Nhựa Dẻo | " + quantity + " tờ";

        } else if ("roi-a4".equals(rawSubtype) || "roi_a4".equals(rawSubtype) || "to_roi_a4".equals(rawSubtype)) {
            int gsm = request.paperGsm() != null ? request.paperGsm() : 150;
            // Match quantity to nearest valid quantity index (500, 1000, 2000, 3000, 5000, 10000)
            int targetQty = quantity >= 10000 ? 10000 : (quantity >= 5000 ? 5000 : (quantity >= 3000 ? 3000 : (quantity >= 2000 ? 2000 : (quantity >= 1000 ? 1000 : 500))));
            configKey = "ROI_A4_" + gsm + "_" + targetQty;

            Optional<PricingConfiguration> configOpt = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("GIAY", configKey);

            if (configOpt.isEmpty()) {
                throw new PricingConfigurationException("Không nhận số lượng hoặc định lượng này cho Tờ rơi A4: " + configKey);
            }

            totalPrice = configOpt.get().getBasePrice().setScale(0, RoundingMode.HALF_UP);
            singleUnitPrice = totalPrice.divide(BigDecimal.valueOf(targetQty), 0, RoundingMode.HALF_UP);
            appliedRules.add("GIAY_" + configKey);
            lineItems.add(new LineItem("ROI_A4", configOpt.get().getConfigName(), totalPrice));
            note = "Tờ Rơi Couche A4 | " + gsm + "g | " + targetQty + " tờ";

        } else if ("roi-a5".equals(rawSubtype) || "roi_a5".equals(rawSubtype) || "to_roi_a5".equals(rawSubtype)) {
            int gsm = request.paperGsm() != null ? request.paperGsm() : 150;
            // Match quantity to nearest valid quantity index (1000, 2000, 4000, 6000, 10000, 20000)
            int targetQty = quantity >= 20000 ? 20000 : (quantity >= 10000 ? 10000 : (quantity >= 6000 ? 6000 : (quantity >= 4000 ? 4000 : (quantity >= 2000 ? 2000 : 1000))));
            configKey = "ROI_A5_" + gsm + "_" + targetQty;

            Optional<PricingConfiguration> configOpt = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("GIAY", configKey);

            if (configOpt.isEmpty()) {
                throw new PricingConfigurationException("Không nhận số lượng hoặc định lượng này cho Tờ rơi A5: " + configKey);
            }

            totalPrice = configOpt.get().getBasePrice().setScale(0, RoundingMode.HALF_UP);
            singleUnitPrice = totalPrice.divide(BigDecimal.valueOf(targetQty), 0, RoundingMode.HALF_UP);
            appliedRules.add("GIAY_" + configKey);
            lineItems.add(new LineItem("ROI_A5", configOpt.get().getConfigName(), totalPrice));
            note = "Tờ Rơi Couche A5 | " + gsm + "g | " + targetQty + " tờ";

        } else if ("bangten".equals(rawSubtype) || "bang_ten".equals(rawSubtype)) {
            String loai = request.materialCode() != null && request.materialCode().contains("50") ? "50K" : "35K";
            String colorRange = request.tranhPackage() != null ? request.tranhPackage().toUpperCase() : "LT5";
            configKey = "BANGTEN_" + loai + "_" + colorRange;

            Optional<PricingConfiguration> configOpt = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("GIAY", configKey);

            if (configOpt.isEmpty()) {
                configOpt = pricingConfigurationRepository.findByCategoryCodeAndConfigKeyAndActiveTrue("GIAY", "BANG_TEN");
            }

            singleUnitPrice = configOpt.map(PricingConfiguration::getBasePrice).orElse(new BigDecimal("35000")).setScale(0, RoundingMode.HALF_UP);
            totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("GIAY_" + configKey);
            lineItems.add(new LineItem("BANG_TEN", "Bảng tên cài áo (" + quantity + " cái)", totalPrice));
            note = "Bảng Tên Cài Áo | " + quantity + " cái";

        } else {
            throw new PricingConfigurationException("Subtype in giấy không hợp lệ: " + rawSubtype);
        }

        return new CalculatePriceResponse(
                "GIAY", false, BigDecimal.ZERO, BigDecimal.ZERO, singleUnitPrice, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
