package com.adprintops.pricing;

import com.adprintops.pricing.domain.PricingMaterial;
import com.adprintops.pricing.domain.PricingRule;
import com.adprintops.pricing.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/pricing")
public class AdminPricingController {

    private final PricingService pricingService;

    public AdminPricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    // ==================== RULES ====================

    @GetMapping("/rules")
    public ResponseEntity<List<PricingRule>> getAllRules(@RequestParam(required = false) String categoryCode) {
        return ResponseEntity.ok(pricingService.getAllRules(categoryCode));
    }

    @PostMapping("/rules")
    public ResponseEntity<PricingRule> createRule(@Valid @RequestBody PricingRuleUpsertRequest request) {
        PricingRule created = pricingService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<PricingRule> updateRule(@PathVariable Long id, @Valid @RequestBody PricingRuleUpsertRequest request) {
        PricingRule updated = pricingService.updateRule(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id, @RequestParam(required = false, defaultValue = "Admin") String updatedBy) {
        pricingService.deleteRule(id, updatedBy);
        return ResponseEntity.noContent().build();
    }

    // ==================== MATERIALS ====================

    @GetMapping("/materials")
    public ResponseEntity<List<PricingMaterial>> getAllMaterials(@RequestParam(required = false) String categoryCode) {
        return ResponseEntity.ok(pricingService.getAllMaterials(categoryCode));
    }

    @PostMapping("/materials")
    public ResponseEntity<PricingMaterial> createMaterial(@Valid @RequestBody PricingMaterialUpsertRequest request) {
        PricingMaterial created = pricingService.createMaterial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<PricingMaterial> updateMaterial(@PathVariable Long id, @Valid @RequestBody PricingMaterialUpsertRequest request) {
        PricingMaterial updated = pricingService.updateMaterial(id, request);
        return ResponseEntity.ok(updated);
    }

    // ==================== HISTORY AUDIT ====================

    @GetMapping("/history")
    public ResponseEntity<List<PricingHistoryResponse>> getAuditHistory(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId
    ) {
        return ResponseEntity.ok(pricingService.getAuditHistory(targetType, targetId));
    }
}
