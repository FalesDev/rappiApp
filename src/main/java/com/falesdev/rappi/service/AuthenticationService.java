package com.falesdev.rappi.service;

import com.falesdev.rappi.domain.dto.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.security.RappiUserDetails;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {

    void sendPhoneOtp(String phone);
    AuthResponse validatePhoneOtp(String phone, String otpCode);
    void sendEmailOtp(String email);
    AuthResponse validateEmailOtp(String identifier, String otpCode);
    UserDetails validateToken(String token);
    AuthUser getUserProfile(RappiUserDetails userDetails);
    //AuthResponse registerWithPhone(String phone);
    AuthResponse registerWithGoogle(String idToken);
    void sendGooglePhoneVerificationOtp(String email, String phone);
    AuthResponse verifyGooglePhoneOtp(String email, String otpCode);
}
