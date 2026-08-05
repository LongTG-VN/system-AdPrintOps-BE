package com.adprintops.design.dto;

public record UploadDesignFileRequest(
        String fileType,
        String fileName,
        String filePath,
        Long fileSizeBytes,
        Long uploadedBy
) {}
