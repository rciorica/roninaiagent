package com.ronin.auth;

import com.ronin.users.UserEntity;
import com.ronin.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepo;

    public UserEntity get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl details)) {
            throw new RuntimeException("No authenticated user");
        }

        return userRepo.findByEmail(details.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
