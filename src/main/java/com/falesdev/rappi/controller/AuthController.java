package com.falesdev.rappi.controller;

import com.falesdev.rappi.domain.dto.response.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.request.*;
import com.falesdev.rappi.domain.dto.response.PhoneRegisterResponse;
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

    @PostMapping("/login/phone/otp")
    public ResponseEntity<Void> sendLoginPhoneOtp(@Valid @RequestBody PhoneRequest request) {
        authenticationService.sendLoginPhoneOtp(request.phone());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/phone/otp/verify")
    public ResponseEntity<AuthResponse> validateLoginPhoneOtp(@Valid @RequestBody PhoneValidationRequest request) {
        return ResponseEntity.ok(authenticationService.validateLoginPhoneOtp(
                request.phone(),
                request.code()
        ));
    }

    @PostMapping("/login/email/otp")
    public ResponseEntity<Void> sendLoginEmailOtp(@Valid @RequestBody EmailRequest request) {
        authenticationService.sendLoginEmailOtp(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/email/otp/verify")
    public ResponseEntity<AuthResponse> validateLoginEmailOtp(@Valid @RequestBody EmailValidationRequest request) {
        return ResponseEntity.ok(authenticationService.validateLoginEmailOtp(
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

    @PostMapping("/register/phone/otp")
    public ResponseEntity<Void> sendRegisterPhoneOtp(@Valid @RequestBody PhoneRequest request) {
        authenticationService.sendRegisterPhoneOtp(request.phone());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/phone/otp/verify")
    public ResponseEntity<PhoneRegisterResponse> validateRegisterPhoneOtp(
            @Valid @RequestBody PhoneValidationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.validateRegisterPhoneOtp(
                request.phone(),
                request.code()
        ));
    }

    @PostMapping("/register/phone")
    public ResponseEntity<AuthResponse> registerWithPhone(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.registerWithPhone(request));
    }

    //Receive the token sent to you by the front mobile
    @PostMapping("/register/google")
    public ResponseEntity<AuthResponse> registerWithGoogle(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authenticationService.registerWithGoogle(request.idToken()));
    }

    @PostMapping("/register/google/phone/otp")
    public ResponseEntity<Void> sendRegisterGooglePhoneOtp(
            @Valid @RequestBody PhoneRequest request,
            @AuthenticationPrincipal RappiUserDetails userDetails
    ) {
        authenticationService.sendRegisterGooglePhoneOtp(
                userDetails.getUser().getEmail(),
                request.phone()
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/register/google/phone/otp/verify")
    public ResponseEntity<AuthResponse> verifyRegisterGooglePhoneOtp(
            @Valid @RequestBody PhoneValidationRequest request,
            @AuthenticationPrincipal RappiUserDetails userDetails
    ) {
        AuthResponse response = authenticationService.verifyRegisterGooglePhoneOtp(
                userDetails.getUser().getEmail(),
                request.phone(),
                request.code()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.refreshAccessToken(request.refreshToken()));
    }
}
