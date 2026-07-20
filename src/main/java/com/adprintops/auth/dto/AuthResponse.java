package com.adprintops.auth.dto;

import java.util.Set;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String email,
        String displayName,
        Set<String> roles
) {
}
