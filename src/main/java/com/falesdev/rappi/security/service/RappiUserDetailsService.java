package com.falesdev.rappi.security.service;

import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.repository.mongo.UserRepository;
import com.falesdev.rappi.security.RappiUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

@RequiredArgsConstructor
public class RappiUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not fount with email: " + email));
        return new RappiUserDetails(user, Map.of());
    }
}
