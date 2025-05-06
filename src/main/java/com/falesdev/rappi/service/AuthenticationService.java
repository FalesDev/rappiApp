package com.falesdev.rappi.service;

import com.falesdev.rappi.domain.dto.request.RegisterRequest;
import com.falesdev.rappi.domain.dto.response.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.response.PhoneRegisterResponse;
import com.falesdev.rappi.security.RappiUserDetails;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {

    void sendLoginPhoneOtp(String phone);
    AuthResponse validateLoginPhoneOtp(String phone, String otpCode);
    void sendLoginEmailOtp(String email);
    AuthResponse validateLoginEmailOtp(String identifier, String otpCode);
    UserDetails validateToken(String token);
    AuthUser getUserProfile(RappiUserDetails userDetails);
    void sendRegisterPhoneOtp(String phone);
    PhoneRegisterResponse validateRegisterPhoneOtp(String phone, String otpCode);
    AuthResponse registerWithPhone(RegisterRequest request);
    AuthResponse registerWithGoogle(String idToken);
    void sendRegisterGooglePhoneOtp(String email, String phone);
    AuthResponse verifyRegisterGooglePhoneOtp(String email, String phone, String otpCode);
}
