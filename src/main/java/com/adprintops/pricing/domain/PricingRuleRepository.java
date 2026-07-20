package com.adprintops.pricing.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    @Query("""
        SELECT r FROM PricingRule r 
        WHERE r.categoryCode = :categoryCode 
          AND r.active = true 
          AND :areaSqm >= r.minAreaSqm 
          AND (r.maxAreaSqm IS NULL OR :areaSqm < r.maxAreaSqm)
        ORDER BY r.minAreaSqm DESC
    """)
    List<PricingRule> findMatchingRules(@Param("categoryCode") String categoryCode, @Param("areaSqm") BigDecimal areaSqm);

    List<PricingRule> findByCategoryCodeOrderByMinAreaSqmAsc(String categoryCode);
}
