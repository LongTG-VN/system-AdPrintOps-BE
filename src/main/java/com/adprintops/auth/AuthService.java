package com.adprintops.auth;

import com.adprintops.auth.dto.AuthResponse;
import com.adprintops.auth.dto.LoginRequest;
import com.adprintops.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
