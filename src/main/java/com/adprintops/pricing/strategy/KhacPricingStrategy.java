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
public class KhacPricingStrategy implements PricingStrategy {

    private final PricingConfigurationRepository pricingConfigurationRepository;

    public KhacPricingStrategy(PricingConfigurationRepository pricingConfigurationRepository) {
        this.pricingConfigurationRepository = pricingConfigurationRepository;
    }

    @Override
    public String getCategoryCode() {
        return "KHAC";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        int quantity = request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1;
        String matCode = request.materialCode() != null ? request.materialCode().toLowerCase() : "can";

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        BigDecimal singleUnitPrice;
        String note;

        if ("lua".equals(matCode) || "bang_lua".equals(matCode) || "bang-lua".equals(matCode) || "bang_lua_chan".equals(matCode)) {
            Optional<PricingConfiguration> configOpt = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("KHAC", "BANG_LUA_CHAN");

            if (configOpt.isEmpty()) {
                throw new PricingConfigurationException("Chưa có cấu hình giá Bảng lụa có chân trong CSDL: BANG_LUA_CHAN");
            }

            singleUnitPrice = configOpt.get().getBasePrice().setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("KHAC_BANG_LUA_CHAN");
            lineItems.add(new LineItem("SERVICE_FIXED", configOpt.get().getConfigName(), singleUnitPrice));
            note = "Phụ Phí | " + configOpt.get().getConfigName();

        } else if ("can".equals(matCode) || "lamination".equals(matCode)) {
            BigDecimal width = request.widthM() != null ? request.widthM() : BigDecimal.ONE;
            BigDecimal height = request.heightM() != null ? request.heightM() : BigDecimal.ONE;
            BigDecimal singleArea = width.multiply(height).setScale(4, RoundingMode.HALF_UP);

            Optional<PricingConfiguration> configOpt = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("KHAC", "LAMINATION_SERVICE");

            if (configOpt.isEmpty()) {
                throw new PricingConfigurationException("Chưa có cấu hình giá Cán màng trong CSDL: LAMINATION_SERVICE");
            }

            BigDecimal rate = configOpt.get().getBasePrice();
            singleUnitPrice = singleArea.multiply(rate).setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("KHAC_LAMINATION_SERVICE");
            lineItems.add(new LineItem("SERVICE_LAMINATION", configOpt.get().getConfigName() + " (" + singleArea + "m² @ " + rate + "đ)", singleUnitPrice));
            note = "Phụ Phí | " + configOpt.get().getConfigName() + " (" + singleArea + "m²)";

        } else {
            // Service key lookup strictly from database (NO public client spoofing or hardcoded fallback)
            String configKey = matCode.toUpperCase();
            Optional<PricingConfiguration> configOpt = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("KHAC", configKey);

            if (configOpt.isEmpty()) {
                throw new PricingConfigurationException("Dịch vụ / phụ phí chưa có cấu hình giá trong CSDL: " + configKey);
            }

            singleUnitPrice = configOpt.get().getBasePrice().setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("KHAC_" + configKey);
            lineItems.add(new LineItem("SERVICE_CONFIGURED", configOpt.get().getConfigName(), singleUnitPrice));
            note = "Phụ Phí | " + configOpt.get().getConfigName();
        }

        BigDecimal totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);

        return new CalculatePriceResponse(
                "KHAC", false, BigDecimal.ZERO, BigDecimal.ZERO, singleUnitPrice, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
