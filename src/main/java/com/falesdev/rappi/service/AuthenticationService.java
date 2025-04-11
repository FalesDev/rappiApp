package com.falesdev.rappi.service;

import com.falesdev.rappi.domain.dto.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.request.SignupRequest;
import com.falesdev.rappi.security.RappiUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {

    void sendOtp(String identifier);
    AuthResponse validateOtp(String identifier, String otpCode);
    AuthResponse authenticate(String email, String password);
    AuthResponse register(SignupRequest signupRequest);
    UserDetails validateToken(String token);
    AuthUser getUserProfile(RappiUserDetails userDetails);
    AuthResponse authenticateWithGoogle(String idToken);
    void sendPhoneVerificationOtp(String email, String phone);
    AuthResponse verifyPhoneOtp(String email, String otpCode);
}
