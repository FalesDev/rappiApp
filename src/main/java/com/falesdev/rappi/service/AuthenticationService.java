package com.falesdev.rappi.service;

import com.falesdev.rappi.domain.dto.request.RegisterRequest;
import com.falesdev.rappi.domain.dto.response.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.response.EmailLoginResponse;
import com.falesdev.rappi.security.RappiUserDetails;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {

    AuthResponse verifyOtp(String fireBaseToken);
    EmailLoginResponse verifyEmailLogin(String email);
    AuthResponse register(RegisterRequest request);
    AuthResponse handleGoogleAuth(String fireBaseToken);
    AuthResponse verifyPhoneForGoogleUser(String tempUserId, String phone);
    UserDetails validateToken(String token);
    AuthUser getUserProfile(RappiUserDetails userDetails);
}
