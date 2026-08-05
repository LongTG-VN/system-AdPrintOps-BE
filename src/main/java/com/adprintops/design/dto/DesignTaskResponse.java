package com.adprintops.design.dto;

import java.time.Instant;
import java.util.List;

public record DesignTaskResponse(
        Long id,
        String taskCode,
        Long orderItemId,
        Long designerId,
        String status,
        String priority,
        Instant deadline,
        String designerNote,
        String customerFeedback,
        List<DesignFileResponse> files,
        List<DesignActivityLogResponse> activityLogs,
        Instant createdAt,
        Instant updatedAt
) {}
