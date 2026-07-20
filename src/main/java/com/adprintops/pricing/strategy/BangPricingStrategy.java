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
public class BangPricingStrategy implements PricingStrategy {

    private final PricingConfigurationRepository pricingConfigurationRepository;

    public BangPricingStrategy(PricingConfigurationRepository pricingConfigurationRepository) {
        this.pricingConfigurationRepository = pricingConfigurationRepository;
    }

    @Override
    public String getCategoryCode() {
        return "BANG";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        BigDecimal width = request.widthM() != null ? request.widthM() : new BigDecimal("0.5");
        BigDecimal height = request.heightM() != null ? request.heightM() : new BigDecimal("0.5");
        int quantity = request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1;

        BigDecimal singleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);
        BigDecimal totalArea = singleArea.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_UP);

        // materialCode e.g. "tole-in", "form-in", "alu-cat", "mica-in", etc.
        String matCode = (request.materialCode() != null && !request.materialCode().isBlank())
                ? request.materialCode().toUpperCase().replace("-", "_") : "FORM_IN";

        String tierSuffix;
        String tierName;
        if (singleArea.compareTo(new BigDecimal("0.06")) <= 0) {
            tierSuffix = "_MINI";
            tierName = "≤0.06m²";
        } else if (singleArea.compareTo(new BigDecimal("0.5")) < 0) {
            tierSuffix = "_MID";
            tierName = "<0.5m²";
        } else {
            // >= 0.5m²
            tierSuffix = "_LARGE";
            tierName = "≥0.5m²";
        }

        String configKey = matCode + tierSuffix;

        Optional<PricingConfiguration> configOpt = pricingConfigurationRepository
                .findByCategoryCodeAndConfigKeyAndActiveTrue("BANG", configKey);

        if (configOpt.isEmpty()) {
            throw new PricingConfigurationException("Chưa có cấu hình giá Bảng hiệu khoán cho tổ hợp " + configKey);
        }

        // Fixed lumpsum price per item according to bGia source!
        BigDecimal singleUnitPrice = configOpt.get().getBasePrice().setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        appliedRules.add("BANG_" + configKey);
        lineItems.add(new LineItem("SIGN_BOARD", configOpt.get().getConfigName() + " (" + singleArea + "m² - Khoán " + tierName + ")", singleUnitPrice));

        String note = "Bảng Hiệu Cứng | " + configOpt.get().getConfigName() + " | Diện tích: " + singleArea + "m² | Khoán: " + tierName;

        return new CalculatePriceResponse(
                "BANG", false, singleArea, totalArea, BigDecimal.ZERO, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
