package com.adprintops.pricing.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingHistoryRepository extends JpaRepository<PricingHistory, Long> {
    List<PricingHistory> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);
    List<PricingHistory> findTop50ByOrderByCreatedAtDesc();
}
