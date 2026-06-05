package com.ronin.config;

import com.ronin.users.UserEntity;
import com.ronin.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            UserEntity user = new UserEntity();
            user.setEmail("ronin@example.com");
            user.setPasswordHash(passwordEncoder.encode("ronin123"));
            user.setCompletedProjects(50); // Start with black belt (1st dan)
            userRepository.save(user);
        }
    }
}
