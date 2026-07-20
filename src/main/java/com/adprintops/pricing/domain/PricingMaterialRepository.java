package com.adprintops.pricing.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PricingMaterialRepository extends JpaRepository<PricingMaterial, Long> {

    Optional<PricingMaterial> findByCategoryCodeAndMaterialCodeAndActiveTrue(String categoryCode, String materialCode);

    List<PricingMaterial> findByCategoryCode(String categoryCode);

    List<PricingMaterial> findByCategoryCodeAndActiveTrue(String categoryCode);
}
