package com.falesdev.rappi.controller;

import com.falesdev.rappi.domain.dto.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.request.*;
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

    @PostMapping("/login/phone")
    public ResponseEntity<Void> requestPhoneOtp(@Valid @RequestBody PhoneRequest request) {
        authenticationService.sendPhoneOtp(request.phone());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/phone/validate")
    public ResponseEntity<AuthResponse> validatePhoneOtp(@Valid @RequestBody PhoneValidationRequest request) {
        return ResponseEntity.ok(authenticationService.validatePhoneOtp(
                request.phone(),
                request.code()
        ));
    }

    @PostMapping("/login/email")
    public ResponseEntity<Void> requestEmailOtp(@Valid @RequestBody EmailRequest request) {
        authenticationService.sendEmailOtp(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/email/validate")
    public ResponseEntity<AuthResponse> validateEmailOtp(@Valid @RequestBody EmailValidationRequest request) {
        return ResponseEntity.ok(authenticationService.validateEmailOtp(
                request.email(),
                request.code()
        ));
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

    /*@PostMapping("/register/phone")
    public ResponseEntity<AuthResponse> registerWithPhone(@Valid @RequestBody PhoneRequest request) {
        return ResponseEntity.ok(authenticationService.registerWithPhone(request.phone()));
    }*/

    //Receive the token sent to you by the front mobile
    @PostMapping("/register/google")
    public ResponseEntity<AuthResponse> registerWithGoogle(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authenticationService.registerWithGoogle(request.idToken()));
    }

    @PostMapping("/register/google/phone")
    public ResponseEntity<Void> sendGooglePhoneVerificationOtp(
            @Valid @RequestBody PhoneRequest request,
            @AuthenticationPrincipal RappiUserDetails userDetails
    ) {
        authenticationService.sendGooglePhoneVerificationOtp(
                userDetails.getUser().getEmail(),
                request.phone()
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/register/google/phone/verify")
    public ResponseEntity<AuthResponse> verifyGooglePhoneOtp(
            @Valid @RequestBody OtpVerificationRequest request,
            @AuthenticationPrincipal RappiUserDetails userDetails
    ) {
        AuthResponse response = authenticationService.verifyGooglePhoneOtp(
                userDetails.getUser().getEmail(),
                request.otp()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.refreshAccessToken(request.refreshToken()));
    }
}
