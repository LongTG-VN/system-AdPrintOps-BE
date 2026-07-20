package com.adprintops.pricing;

/**
 * Raised when a quotation cannot be calculated from the approved pricing configuration.
 */
public class PricingConfigurationException extends RuntimeException {

    public PricingConfigurationException(String message) {
        super(message);
    }
}
