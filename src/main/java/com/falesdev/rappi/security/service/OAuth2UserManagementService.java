package com.falesdev.rappi.security.service;

import com.falesdev.rappi.domain.LoginType;
import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.document.User;
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
        Role defaultRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new DocumentNotFoundException("Role CUSTOMER not found"));

        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.setName(name);
                    existingUser.setImageURL(picture);
                    existingUser.setLoginType(LoginType.valueOf(provider.toUpperCase()));
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .imageURL(picture)
                                .role(defaultRole)
                                .loginType(LoginType.valueOf(provider.toUpperCase()))
                                .password(null)
                                .build()
                ));
    }
}
