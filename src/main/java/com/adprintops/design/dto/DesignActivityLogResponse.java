package com.adprintops.design.dto;

import java.time.Instant;

public record DesignActivityLogResponse(
        Long id,
        Long designTaskId,
        Long actorId,
        String actionType,
        String content,
        Instant createdAt
) {}
