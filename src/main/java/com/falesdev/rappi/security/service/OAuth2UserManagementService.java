package com.falesdev.rappi.security.service;

import com.falesdev.rappi.domain.RegisterType;
import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.exception.AuthenticationMethodConflictException;
import com.falesdev.rappi.exception.BadRequestException;
import com.falesdev.rappi.exception.DocumentNotFoundException;
import com.falesdev.rappi.repository.mongo.RoleRepository;
import com.falesdev.rappi.repository.mongo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User createOrUpdateOAuth2User(
            String email, String firstName, String lastName, String picture, String provider) {

        RegisterType registerType;
        try {
            registerType = RegisterType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported OAuth2 provider: " + provider);
        }

        Role defaultRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new DocumentNotFoundException("Role CUSTOMER not found"));

        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    if (existingUser.getRegisterType() != registerType) {
                        throw new AuthenticationMethodConflictException("Account registered with "
                                + existingUser.getRegisterType());
                    }
                    existingUser.setFirstName(firstName);
                    existingUser.setLastName(lastName);
                    existingUser.setImageURL(picture);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .firstName(firstName)
                                .lastName(lastName)
                                .imageURL(picture)
                                .role(defaultRole)
                                .registerType(registerType)
                                .phoneVerified(false)
                                .password(null)
                                .build()
                ));
    }
}
