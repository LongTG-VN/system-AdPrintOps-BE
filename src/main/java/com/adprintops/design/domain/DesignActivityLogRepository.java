package com.adprintops.design.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignActivityLogRepository extends JpaRepository<DesignActivityLog, Long> {
    List<DesignActivityLog> findByDesignTaskIdOrderByCreatedAtDesc(Long designTaskId);
}
