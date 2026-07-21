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

@Component
public class TranhPricingStrategy implements PricingStrategy {

    private final PricingConfigurationRepository pricingConfigurationRepository;

    public TranhPricingStrategy(PricingConfigurationRepository pricingConfigurationRepository) {
        this.pricingConfigurationRepository = pricingConfigurationRepository;
    }

    @Override
    public String getCategoryCode() {
        return "TRANH";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        String tranhType = request.tranhType() != null ? request.tranhType().toLowerCase() : "tranh_dien";
        int quantity = request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1;

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        BigDecimal singleUnitPrice;
        String note;

        if ("so_nha".equals(tranhType) || "so-nha".equals(tranhType)) {
            // Biển số nhà: size (25X15, 30X20, 35X25, 40X30, 60X40) x type (ANMON, NOI, CHUNOI)
            String rawPreset = request.tranhPreset() != null ? request.tranhPreset().toUpperCase() : "30X20";
            String size = (rawPreset.contains("25X15") || rawPreset.contains("30X20") || rawPreset.contains("35X25") || rawPreset.contains("40X30") || rawPreset.contains("60X40"))
                    ? rawPreset
                    : "30X20";

            String rawType = (request.tranhPackage() != null && !request.tranhPackage().isBlank())
                    ? request.tranhPackage()
                    : (request.materialCode() != null ? request.materialCode() : "ANMON");

            String normalizedType = rawType.toUpperCase().replace("-", "_").replace("AN_MON", "ANMON").replace("SO_NOI", "NOI").replace("CHU_SO_NOI", "CHUNOI");
            String type = (normalizedType.contains("ANMON") || normalizedType.contains("NOI") || normalizedType.contains("CHUNOI"))
                    ? normalizedType
                    : "ANMON";

            String configKey = rawType.toUpperCase().startsWith("SN_") ? rawType.toUpperCase() : "SN_" + size + "_" + type;

            PricingConfiguration config = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("TRANH", configKey)
                    .orElseThrow(() -> new PricingConfigurationException(
                            "Chưa có cấu hình giá biển số nhà cho tổ hợp " + configKey
                    ));

            singleUnitPrice = config.getBasePrice().setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("TRANH_" + configKey);
            lineItems.add(new LineItem("SO_NHA", config.getConfigName(), singleUnitPrice));
            note = "Biển Số Nhà 3D | " + config.getConfigName();

        } else {
            // Tranh điện LED: 9 presets x 2 packages (FULL, IN)
            String configKey;
            String rawPkg = request.tranhPackage() != null ? request.tranhPackage().toUpperCase() : "";

            if (rawPkg.startsWith("LED_")) {
                configKey = rawPkg;
            } else {
                String preset = request.tranhPreset() != null ? request.tranhPreset().toUpperCase() : "A4";
                String pkg = rawPkg.isBlank() || (!"FULL".equals(rawPkg) && !"IN".equals(rawPkg)) ? "FULL" : rawPkg;
                configKey = "LED_" + preset + "_" + pkg;
            }

            PricingConfiguration config = pricingConfigurationRepository
                    .findByCategoryCodeAndConfigKeyAndActiveTrue("TRANH", configKey)
                    .orElseThrow(() -> new PricingConfigurationException(
                            "Chưa có cấu hình giá tranh điện cho tổ hợp " + configKey
                    ));

            singleUnitPrice = config.getBasePrice().setScale(0, RoundingMode.HALF_UP);
            appliedRules.add("TRANH_" + configKey);
            lineItems.add(new LineItem("TRANH_LED", config.getConfigName(), singleUnitPrice));
            note = "Tranh Điện LED | " + config.getConfigName();
        }

        BigDecimal totalPrice = singleUnitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(0, RoundingMode.HALF_UP);

        return new CalculatePriceResponse(
                "TRANH", false, BigDecimal.ZERO, BigDecimal.ZERO, singleUnitPrice, BigDecimal.ZERO, singleUnitPrice, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
