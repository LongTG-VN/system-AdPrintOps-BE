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

        BigDecimal boardCost = configOpt.get().getBasePrice().setScale(0, RoundingMode.HALF_UP);

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        appliedRules.add("BANG_" + configKey);
        lineItems.add(new LineItem("SIGN_BOARD", configOpt.get().getConfigName() + " (" + singleArea + "m² - Khoán " + tierName + ")", boardCost.multiply(BigDecimal.valueOf(quantity))));

        // Frame calculation: 0 (Không khung), 16 (65k/m), 20 (85k/m), 25 (105k/m)
        int tubeSize = request.frameTubeSize() != null ? request.frameTubeSize() : 0;
        BigDecimal tienSat = BigDecimal.ZERO;
        BigDecimal tienChan = BigDecimal.ZERO;

        if (tubeSize > 0) {
            BigDecimal frameRatePerM = (tubeSize == 25) ? new BigDecimal("105000") : ((tubeSize == 20) ? new BigDecimal("85000") : new BigDecimal("65000"));
            BigDecimal chuViPerItem = width.add(height).multiply(new BigDecimal("2"));
            tienSat = chuViPerItem.multiply(frameRatePerM).multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);
            lineItems.add(new LineItem("FRAME", "Khung sắt " + tubeSize + "mm (" + chuViPerItem + "m @ " + frameRatePerM + "đ/m)", tienSat));
            appliedRules.add("BANG_FRAME_V" + tubeSize);

            if (Boolean.TRUE.equals(request.hasLeg())) {
                BigDecimal totalLegM = new BigDecimal("4.0").multiply(BigDecimal.valueOf(quantity));
                tienChan = frameRatePerM.multiply(totalLegM).setScale(0, RoundingMode.HALF_UP);
                lineItems.add(new LineItem("FRAME_LEGS", "Thêm 2 chân khung (4m @ " + frameRatePerM + "đ/m)", tienChan));
                appliedRules.add("BANG_FRAME_LEGS_4M");
            }
        }

        BigDecimal totalPrice = boardCost.multiply(BigDecimal.valueOf(quantity)).add(tienSat).add(tienChan).setScale(0, RoundingMode.HALF_UP);
        BigDecimal singleUnitPrice = totalPrice.divide(BigDecimal.valueOf(quantity), 0, RoundingMode.HALF_UP);

        String frameNote = tubeSize > 0 ? " | Khung sắt V" + tubeSize : " | Không khung sắt";
        String note = "Bảng Hiệu | " + configOpt.get().getConfigName() + frameNote + " | " + singleArea + "m²";

        return new CalculatePriceResponse(
                "BANG", false, singleArea, totalArea, BigDecimal.ZERO, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
