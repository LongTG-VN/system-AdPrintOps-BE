package com.adprintops.design.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignFileRepository extends JpaRepository<DesignFile, Long> {
    List<DesignFile> findByDesignTaskIdOrderByVersionNumberDesc(Long designTaskId);
}
