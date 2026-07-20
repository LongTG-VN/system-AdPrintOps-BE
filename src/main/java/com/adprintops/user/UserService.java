package com.adprintops.user;

import com.adprintops.user.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile(String email);
}
