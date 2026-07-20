package com.adprintops.pricing.strategy;

import com.adprintops.pricing.dto.CalculatePriceRequest;
import com.adprintops.pricing.dto.CalculatePriceResponse;

public interface PricingStrategy {
    String getCategoryCode();
    CalculatePriceResponse calculate(CalculatePriceRequest request);
}
