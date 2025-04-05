package com.falesdev.rappi.security;

import com.falesdev.rappi.domain.document.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;

import java.util.Collections;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class RappiUserDetails implements UserDetails, OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    // Métodos de UserDetails
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // Puede ser null para usuarios OAuth2
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Métodos de OAuth2User
    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())
        );
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    public String getId() {
        return user.getId();
    }
}
