package com.adprintops.design.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignTaskRepository extends JpaRepository<DesignTask, Long> {
    Optional<DesignTask> findByTaskCode(String taskCode);
    List<DesignTask> findByDesignerId(Long designerId);
    List<DesignTask> findByStatus(String status);
    Optional<DesignTask> findByOrderItemId(Long orderItemId);
}
