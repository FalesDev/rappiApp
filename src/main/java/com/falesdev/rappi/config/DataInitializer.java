package com.falesdev.rappi.config;

import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.repository.RoleRepository;
import com.falesdev.rappi.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    public CommandLineRunner initializeData() {
        return args -> {
            Role customerRole = createRoleIfNotFound("CUSTOMER");
            Role deliveryRole = createRoleIfNotFound("DELIVERY");
            Role restaurantOwnerRole = createRoleIfNotFound("RESTAURANT_OWNER");
            Role adminRole = createRoleIfNotFound("ADMIN");

            createUserIfNotFound("admin@test.com", "Admin User", "adminpassword", adminRole);
            createUserIfNotFound("user@test.com", "Test User", "password", customerRole);
        };
    }

    private Role createRoleIfNotFound(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    log.info("Creating rol: {}", name);
                    return roleRepository.save(
                            Role.builder()
                                    .name(name)
                                    .build()
                    );
                });
    }

    private void createUserIfNotFound(String email, String name,
                                      String rawPassword, Role role) {
        userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Creating user: {}", email);
            return userRepository.save(
                    User.builder()
                            .email(email)
                            .name(name)
                            .password(passwordEncoder.encode(rawPassword))
                            .role(role)
                            .build()
            );
        });
    }
}
