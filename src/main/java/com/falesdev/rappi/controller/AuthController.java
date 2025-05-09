package com.falesdev.rappi.controller;

import com.falesdev.rappi.domain.dto.response.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.request.*;
import com.falesdev.rappi.domain.dto.response.EmailLoginResponse;
import com.falesdev.rappi.security.RappiUserDetails;
import com.falesdev.rappi.service.AuthenticationService;
import com.falesdev.rappi.service.JwtService;
import com.falesdev.rappi.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private  final RefreshTokenService refreshTokenService;

    @PostMapping("/phone/verify")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody FirebaseVerificationRequest request
    ) {
        return ResponseEntity.ok(
                authenticationService.verifyOtp(
                        request.firebaseToken()
                )
        );
    }

    @PostMapping("/email")
    public ResponseEntity<EmailLoginResponse> verifyEmailLogin(
            @Valid @RequestBody EmailRequest request
    ) {
        return ResponseEntity.ok(
                authenticationService.verifyEmailLogin(
                        request.email()
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(
            @Valid @RequestBody FirebaseVerificationRequest request
    ) {
        return ResponseEntity.ok(
                authenticationService.handleGoogleAuth(request.firebaseToken())
        );
    }

    @PostMapping("/google/phone/verify")
    public ResponseEntity<AuthResponse> verifyGooglePhone(
            @Valid @RequestBody GooglePhoneVerificationRequest request
    ) {
        return ResponseEntity.ok(
                authenticationService.verifyPhoneForGoogleUser(
                        request.userId(),
                        request.phone()
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUser> getUserProfile(
            @AuthenticationPrincipal RappiUserDetails userDetails
    ) {
        return ResponseEntity.ok(authenticationService.getUserProfile(userDetails));
    }

    //This will be removed later because the token will be redirected to the frontend
    @GetMapping("/oauth-success")
    public ResponseEntity<AuthResponse> oauthSuccess(
            @RequestParam String token,
            @RequestParam String refreshToken
    ) {
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getExpirationTime(token))
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.refreshAccessToken(request.refreshToken()));
    }
}
