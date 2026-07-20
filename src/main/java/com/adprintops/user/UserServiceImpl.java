package com.adprintops.user;

import com.adprintops.user.dto.UserProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(UserNotFoundException::new);
        Set<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        return new UserProfileResponse(user.getEmail(), user.getDisplayName(), roles);
    }
}
