package com.adprintops.design.dto;

public record AssignDesignerRequest(
        Long designerId,
        String priority,
        Long actorId
) {}
