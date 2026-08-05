package com.adprintops.design.dto;

public record UpdateDesignStatusRequest(
        String status,
        String note,
        Long actorId
) {}
