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
public class CardPricingStrategy implements PricingStrategy {
    private final PricingConfigurationRepository configurations;

    public CardPricingStrategy(PricingConfigurationRepository configurations) { this.configurations = configurations; }

    @Override
    public String getCategoryCode() {
        return "CARD";
    }

    @Override
    public CalculatePriceResponse calculate(CalculatePriceRequest request) {
        int boxes = request.boxCount() != null && request.boxCount() > 0 ? request.boxCount()
                : (request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1);

        List<LineItem> lineItems = new ArrayList<>();
        List<String> appliedRules = new ArrayList<>();

        String ruleName;

        if (boxes == 5) {
            ruleName = "CARD_EXACT_5_BOXES_PROMO";
        } else if (boxes >= 10) {
            ruleName = "CARD_BULK_GE_10_BOXES";
        } else {
            // 1-4 boxes or 6-9 boxes -> 70k/box
            ruleName = "CARD_STANDARD_1-4_OR_6-9_BOXES";
        }

        String key = boxes == 5 ? "BOX_5" : boxes >= 10 ? "BOX_10_PLUS" : boxes <= 4 ? "BOX_1_4" : "BOX_6_9";
        PricingConfiguration config = configurations.findByCategoryCodeAndConfigKeyAndActiveTrue("CARD", key)
                .orElseThrow(() -> new PricingConfigurationException("Thiếu cấu hình giá CARD: " + key));
        BigDecimal pricePerBox = config.getBasePrice();

        appliedRules.add(ruleName);
        BigDecimal totalPrice = pricePerBox.multiply(BigDecimal.valueOf(boxes)).setScale(0, RoundingMode.HALF_UP);
        lineItems.add(new LineItem("PRINT_CARD", "Card Visit (" + boxes + " hộp @ " + pricePerBox + "đ/hộp)", totalPrice));

        String note = "Card Visit | " + boxes + " hộp | " + pricePerBox + "đ/hộp";

        return new CalculatePriceResponse(
                "CARD", false, BigDecimal.ZERO, BigDecimal.ZERO, pricePerBox, BigDecimal.ZERO, pricePerBox, totalPrice, "VND", lineItems, appliedRules, note
        );
    }
}
