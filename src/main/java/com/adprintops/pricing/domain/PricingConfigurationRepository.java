package com.adprintops.pricing.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingConfigurationRepository extends JpaRepository<PricingConfiguration, Long> {
    List<PricingConfiguration> findByCategoryCodeAndActiveTrue(String categoryCode);
    List<PricingConfiguration> findByCategoryCode(String categoryCode);
    Optional<PricingConfiguration> findByCategoryCodeAndConfigKeyAndActiveTrue(String categoryCode, String configKey);
}
