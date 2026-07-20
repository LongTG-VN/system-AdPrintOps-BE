package com.adprintops.auth;

import com.adprintops.auth.dto.AuthResponse;
import com.adprintops.auth.dto.LoginRequest;
import com.adprintops.auth.dto.RegisterRequest;
import com.adprintops.user.Role;
import com.adprintops.user.RoleRepository;
import com.adprintops.user.User;
import com.adprintops.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "CUSTOMER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        Role customerRole = roleRepository.findByCode(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Thiếu role mặc định CUSTOMER trong CSDL."));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setActive(true);
        user.setRoles(new HashSet<>(Set.of(customerRole)));

        return toAuthResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        Set<String> roles = user.getRoles().stream().map(Role::getCode).collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new AuthResponse(
                jwtTokenProvider.generateToken(user),
                "Bearer",
                jwtTokenProvider.getExpirationSeconds(),
                user.getEmail(),
                user.getDisplayName(),
                roles
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
