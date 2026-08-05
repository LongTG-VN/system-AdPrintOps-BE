package com.adprintops.design.dto;

import java.time.Instant;

public record DesignFileResponse(
        Long id,
        Long designTaskId,
        Integer versionNumber,
        String fileType,
        String fileName,
        String filePath,
        Long fileSizeBytes,
        Long uploadedBy,
        boolean approved,
        Instant createdAt
) {}
