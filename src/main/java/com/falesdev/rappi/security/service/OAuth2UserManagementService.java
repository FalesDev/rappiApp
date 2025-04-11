package com.falesdev.rappi.security.service;

import com.falesdev.rappi.domain.LoginType;
import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.exception.AuthenticationMethodConflictException;
import com.falesdev.rappi.exception.BadRequestException;
import com.falesdev.rappi.exception.DocumentNotFoundException;
import com.falesdev.rappi.repository.RoleRepository;
import com.falesdev.rappi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User createOrUpdateOAuth2User(String email, String name, String picture, String provider) {

        LoginType loginType;
        try {
            loginType = LoginType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported OAuth2 provider: " + provider);
        }

        Role defaultRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new DocumentNotFoundException("Role CUSTOMER not found"));

        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    if (existingUser.getLoginType() != loginType) {
                        throw new AuthenticationMethodConflictException("Account registered with " + existingUser.getLoginType());
                    }
                    existingUser.setName(name);
                    existingUser.setImageURL(picture);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .imageURL(picture)
                                .role(defaultRole)
                                .loginType(loginType)
                                .phoneVerified(false)
                                .password(null)
                                .build()
                ));
    }
}
