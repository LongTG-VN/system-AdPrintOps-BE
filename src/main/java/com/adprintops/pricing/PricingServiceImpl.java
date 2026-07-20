package com.adprintops.pricing;

import com.adprintops.pricing.domain.*;
import com.adprintops.pricing.dto.*;
import com.adprintops.pricing.strategy.PricingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PricingServiceImpl implements PricingService {

    private final PricingRuleRepository pricingRuleRepository;
    private final PricingMaterialRepository pricingMaterialRepository;
    private final PricingHistoryRepository pricingHistoryRepository;
    private final Map<String, PricingStrategy> strategyMap;
    private final boolean allowFallback;

    public PricingServiceImpl(PricingRuleRepository pricingRuleRepository,
                              PricingMaterialRepository pricingMaterialRepository,
                              PricingHistoryRepository pricingHistoryRepository,
                              List<PricingStrategy> strategies,
                              @Value("${app.pricing.allow-fallback:false}") boolean allowFallback) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.pricingMaterialRepository = pricingMaterialRepository;
        this.pricingHistoryRepository = pricingHistoryRepository;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getCategoryCode().toUpperCase(),
                        Function.identity()
                ));
        this.allowFallback = allowFallback;
    }

    @Override
    @Transactional(readOnly = true)
    public CalculatePriceResponse calculatePrice(CalculatePriceRequest request) {
        if (request.categoryCode() == null || request.categoryCode().isBlank()) {
            throw new IllegalArgumentException("Mã danh mục (categoryCode) không được để trống.");
        }

        String catCode = request.categoryCode().toUpperCase();
        PricingStrategy strategy = strategyMap.get(catCode);

        if (strategy == null) {
            throw new PricingConfigurationException("Chưa hỗ trợ động cơ tính giá cho danh mục: " + catCode);
        }

        return strategy.calculate(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingMaterial> getActiveMaterials(String categoryCode) {
        String code = (categoryCode != null && !categoryCode.isBlank()) ? categoryCode.toUpperCase() : "DECAL";
        return pricingMaterialRepository.findByCategoryCodeAndActiveTrue(code);
    }

    @Override
    @Transactional(readOnly = true)
    public DecalPriceResponse calculateDecalPrice(DecalPriceRequest request) {
        CalculatePriceRequest unifiedReq = new CalculatePriceRequest(
                "DECAL",
                request.widthM(),
                request.heightM(),
                request.quantity(),
                request.decalType(),
                request.hasLamination(),
                false, null, null, null, null, null
        );

        CalculatePriceResponse resp = calculatePrice(unifiedReq);

        return new DecalPriceResponse(
                resp.singleAreaSqm(),
                resp.totalAreaSqm(),
                resp.ratePerSqm(),
                resp.laminationCost(),
                resp.singleUnitPrice(),
                resp.totalPrice(),
                "IN_DECAL",
                resp.breakdownNote()
        );
    }

    // ==================== ADMIN RULE CRUD & AUDIT ====================

    @Override
    @Transactional(readOnly = true)
    public List<PricingRule> getAllRules(String categoryCode) {
        if (categoryCode != null && !categoryCode.isBlank()) {
            return pricingRuleRepository.findByCategoryCodeOrderByMinAreaSqmAsc(categoryCode.toUpperCase());
        }
        return pricingRuleRepository.findAll();
    }

    @Override
    @Transactional
    public PricingRule createRule(PricingRuleUpsertRequest request) {
        String catCode = request.categoryCode().toUpperCase();
        if (request.active()) {
            validateRuleOverlap(null, catCode, request.minAreaSqm(), request.maxAreaSqm());
        }

        PricingRule rule = new PricingRule(
                catCode,
                request.ruleName(),
                request.minAreaSqm(),
                request.maxAreaSqm(),
                request.pricePerSqm(),
                request.laminationFeePerSqm(),
                request.active(),
                request.note()
        );
        PricingRule saved = pricingRuleRepository.save(rule);
        logAudit("RULE", saved.getId(), "CREATE", null, saved.getRuleName() + " (" + saved.getPricePerSqm() + "đ/m²)", getAuthenticatedUser());
        return saved;
    }

    @Override
    @Transactional
    public PricingRule updateRule(Long id, PricingRuleUpsertRequest request) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PricingRule not found with id: " + id));

        String catCode = request.categoryCode().toUpperCase();
        if (request.active()) {
            validateRuleOverlap(id, catCode, request.minAreaSqm(), request.maxAreaSqm());
        }

        String user = getAuthenticatedUser();

        if (rule.getPricePerSqm().compareTo(request.pricePerSqm()) != 0) {
            logAudit("RULE", id, "price_per_sqm", rule.getPricePerSqm().toString(), request.pricePerSqm().toString(), user);
            rule.setPricePerSqm(request.pricePerSqm());
        }
        if (rule.getLaminationFeePerSqm().compareTo(request.laminationFeePerSqm()) != 0) {
            logAudit("RULE", id, "lamination_fee_per_sqm", rule.getLaminationFeePerSqm().toString(), request.laminationFeePerSqm().toString(), user);
            rule.setLaminationFeePerSqm(request.laminationFeePerSqm());
        }
        if (rule.isActive() != request.active()) {
            logAudit("RULE", id, "is_active", String.valueOf(rule.isActive()), String.valueOf(request.active()), user);
            rule.setActive(request.active());
        }
        if (!Objects.equals(rule.getRuleName(), request.ruleName())) {
            logAudit("RULE", id, "rule_name", rule.getRuleName(), request.ruleName(), user);
            rule.setRuleName(request.ruleName());
        }
        if (rule.getMinAreaSqm().compareTo(request.minAreaSqm()) != 0) {
            logAudit("RULE", id, "min_area_sqm", rule.getMinAreaSqm().toString(), request.minAreaSqm().toString(), user);
            rule.setMinAreaSqm(request.minAreaSqm());
        }
        if (!Objects.equals(rule.getMaxAreaSqm(), request.maxAreaSqm())) {
            logAudit("RULE", id, "max_area_sqm", String.valueOf(rule.getMaxAreaSqm()), String.valueOf(request.maxAreaSqm()), user);
            rule.setMaxAreaSqm(request.maxAreaSqm());
        }
        if (!Objects.equals(rule.getNote(), request.note())) {
            logAudit("RULE", id, "note", rule.getNote(), request.note(), user);
            rule.setNote(request.note());
        }

        return pricingRuleRepository.save(rule);
    }

    private void validateRuleOverlap(Long currentId, String categoryCode, BigDecimal minArea, BigDecimal maxArea) {
        if (maxArea != null && maxArea.compareTo(minArea) <= 0) {
            throw new IllegalArgumentException("Diện tích tối đa (maxAreaSqm) phải lớn hơn diện tích tối thiểu (minAreaSqm).");
        }

        List<PricingRule> activeRules = pricingRuleRepository.findByCategoryCodeOrderByMinAreaSqmAsc(categoryCode);
        for (PricingRule existing : activeRules) {
            if (!existing.isActive()) continue;
            if (currentId != null && existing.getId().equals(currentId)) continue;

            boolean overlap = isOverlap(minArea, maxArea, existing.getMinAreaSqm(), existing.getMaxAreaSqm());
            if (overlap) {
                throw new PricingConfigurationException(
                        "Khoảng diện tích [" + minArea + "m² - " + (maxArea != null ? maxArea + "m²" : "∞") + 
                        "] bị chồng chéo với quy tắc '" + existing.getRuleName() + 
                        "' [" + existing.getMinAreaSqm() + "m² - " + (existing.getMaxAreaSqm() != null ? existing.getMaxAreaSqm() + "m²" : "∞") + "]."
                );
            }
        }
    }

    private boolean isOverlap(BigDecimal minA, BigDecimal maxA, BigDecimal minB, BigDecimal maxB) {
        boolean minALessThanMaxB = (maxB == null) || (minA.compareTo(maxB) < 0);
        boolean minBLessThanMaxA = (maxA == null) || (minB.compareTo(maxA) < 0);
        return minALessThanMaxB && minBLessThanMaxA;
    }

    @Override
    @Transactional
    public void deleteRule(Long id, String updatedBy) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PricingRule not found with id: " + id));
        rule.setActive(false);
        pricingRuleRepository.save(rule);
        logAudit("RULE", id, "is_active", "true", "false", getAuthenticatedUser());
    }

    // ==================== ADMIN MATERIAL CRUD & AUDIT ====================

    @Override
    @Transactional(readOnly = true)
    public List<PricingMaterial> getAllMaterials(String categoryCode) {
        if (categoryCode != null && !categoryCode.isBlank()) {
            return pricingMaterialRepository.findByCategoryCode(categoryCode.toUpperCase());
        }
        return pricingMaterialRepository.findAll();
    }

    @Override
    @Transactional
    public PricingMaterial createMaterial(PricingMaterialUpsertRequest request) {
        PricingMaterial material = new PricingMaterial(
                request.categoryCode().toUpperCase(),
                request.materialCode().toLowerCase(),
                request.materialName(),
                request.multiplier(),
                request.basePrice(),
                request.active()
        );
        PricingMaterial saved = pricingMaterialRepository.save(material);
        logAudit("MATERIAL", saved.getId(), "CREATE", null, saved.getMaterialName() + " (Multiplier " + saved.getMultiplier() + ")", getAuthenticatedUser());
        return saved;
    }

    @Override
    @Transactional
    public PricingMaterial updateMaterial(Long id, PricingMaterialUpsertRequest request) {
        PricingMaterial material = pricingMaterialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PricingMaterial not found with id: " + id));

        String user = getAuthenticatedUser();

        if (material.getMultiplier().compareTo(request.multiplier()) != 0) {
            logAudit("MATERIAL", id, "multiplier", material.getMultiplier().toString(), request.multiplier().toString(), user);
            material.setMultiplier(request.multiplier());
        }
        if (material.getBasePrice().compareTo(request.basePrice()) != 0) {
            logAudit("MATERIAL", id, "base_price", material.getBasePrice().toString(), request.basePrice().toString(), user);
            material.setBasePrice(request.basePrice());
        }
        if (material.isActive() != request.active()) {
            logAudit("MATERIAL", id, "is_active", String.valueOf(material.isActive()), String.valueOf(request.active()), user);
            material.setActive(request.active());
        }
        if (!Objects.equals(material.getMaterialName(), request.materialName())) {
            logAudit("MATERIAL", id, "material_name", material.getMaterialName(), request.materialName(), user);
            material.setMaterialName(request.materialName());
        }

        return pricingMaterialRepository.save(material);
    }

    // ==================== AUDIT HISTORY ====================

    @Override
    @Transactional(readOnly = true)
    public List<PricingHistoryResponse> getAuditHistory(String targetType, Long targetId) {
        List<PricingHistory> histories;
        if (targetType != null && targetId != null) {
            histories = pricingHistoryRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);
        } else {
            histories = pricingHistoryRepository.findTop50ByOrderByCreatedAtDesc();
        }
        return histories.stream()
                .map(h -> new PricingHistoryResponse(
                        h.getId(),
                        h.getTargetType(),
                        h.getTargetId(),
                        h.getFieldName(),
                        h.getOldValue(),
                        h.getNewValue(),
                        h.getChangedBy(),
                        h.getCreatedAt()
                ))
                .toList();
    }

    private void logAudit(String targetType, Long targetId, String fieldName, String oldValue, String newValue, String changedBy) {
        PricingHistory history = new PricingHistory(targetType, targetId, fieldName, oldValue, newValue, changedBy != null ? changedBy : "Admin");
        pricingHistoryRepository.save(history);
    }

    private String getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "Admin";
    }
}
