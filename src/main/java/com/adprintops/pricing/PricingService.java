package com.adprintops.pricing;

import com.adprintops.pricing.domain.PricingMaterial;
import com.adprintops.pricing.domain.PricingRule;
import com.adprintops.pricing.dto.*;

import java.util.List;

public interface PricingService {
    DecalPriceResponse calculateDecalPrice(DecalPriceRequest request);

    // Unified Multi-Category Strategy Calculation
    CalculatePriceResponse calculatePrice(CalculatePriceRequest request);

    // Public Material Config
    List<PricingMaterial> getActiveMaterials(String categoryCode);

    // Admin Rule Management
    List<PricingRule> getAllRules(String categoryCode);
    PricingRule createRule(PricingRuleUpsertRequest request);
    PricingRule updateRule(Long id, PricingRuleUpsertRequest request);
    void deleteRule(Long id, String updatedBy);

    // Admin Material Management
    List<PricingMaterial> getAllMaterials(String categoryCode);
    PricingMaterial createMaterial(PricingMaterialUpsertRequest request);
    PricingMaterial updateMaterial(Long id, PricingMaterialUpsertRequest request);

    // Audit History
    List<PricingHistoryResponse> getAuditHistory(String targetType, Long targetId);
}
