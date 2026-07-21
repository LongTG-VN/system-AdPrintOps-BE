package com.adprintops.common;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        Map<String, String> fieldErrors
) {
    public ApiError(String code, String message) {
        this(Instant.now(), 401, code, message, null);
    }
}
