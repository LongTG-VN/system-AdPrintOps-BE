package com.adprintops.user.dto;

import java.util.Set;

public record UserProfileResponse(
        String email,
        String displayName,
        Set<String> roles
) {
}
