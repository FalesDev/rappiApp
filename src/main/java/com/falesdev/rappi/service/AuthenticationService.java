package com.falesdev.rappi.service;

import com.falesdev.rappi.domain.dto.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.GoogleLoginRequest;
import com.falesdev.rappi.domain.dto.request.SignupRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {

    AuthResponse authenticate(String email, String password);
    AuthResponse register(SignupRequest signupRequest);
    UserDetails validateToken(String token);
    AuthUser getUserProfile(Authentication authentication);
    AuthResponse authenticateWithGoogle(String idToken);
}
