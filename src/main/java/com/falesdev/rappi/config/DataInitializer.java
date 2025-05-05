package com.falesdev.rappi.config;

import com.falesdev.rappi.domain.RegisterType;
import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.repository.mongo.RoleRepository;
import com.falesdev.rappi.repository.mongo.UserRepository;
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

            createUserIfNotFound("admin@test.com", "Admin User",
                    "adminpassword", "+51123456789",adminRole);
            createUserIfNotFound("user@test.com", "Test User",
                    "password","+51123456780",customerRole);
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

    private void createUserIfNotFound(String email, String firstName,
                                      String rawPassword, String phone, Role role) {
        userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Creating user: {}", email);
            return userRepository.save(
                    User.builder()
                            .email(email)
                            .firstName(firstName)
                            .password(passwordEncoder.encode(rawPassword))
                            .phone(phone)
                            .phoneVerified(true)
                            .role(role)
                            .registerType(RegisterType.LOCAL)
                            .build()
            );
        });
    }
}
