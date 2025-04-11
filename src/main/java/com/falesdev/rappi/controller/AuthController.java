package com.falesdev.rappi.controller;

import com.falesdev.rappi.domain.document.User;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private  final RefreshTokenService refreshTokenService;

    @PostMapping("/login/otp/request")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody OtpRequest request) {
        authenticationService.sendOtp(request.identifier());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/otp/validate")
    public ResponseEntity<AuthResponse> validateOtp(@Valid @RequestBody OtpValidationRequest request) {
        return ResponseEntity.ok(authenticationService.validateOtp(
                request.identifier(),
                request.code()
        ));
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUser> getUserProfile(
            @AuthenticationPrincipal RappiUserDetails userDetails // ✅
    ) {
        return ResponseEntity.ok(authenticationService.getUserProfile(userDetails));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authenticationService.register(signupRequest));
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

    //Receive the token sent to you by the front mobile
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateWithGoogle(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticateWithGoogle(request.idToken()));
    }

    @PostMapping("google/phone")
    public ResponseEntity<Void> sendPhoneVerificationOtp(
            @Valid @RequestBody PhoneRequest request,
            @AuthenticationPrincipal RappiUserDetails userDetails
    ) {
        authenticationService.sendPhoneVerificationOtp(
                userDetails.getUser().getEmail(),
                request.phone()
        );
        return ResponseEntity.accepted().build();
    }

    @PostMapping("google/phone/verify")
    public ResponseEntity<AuthResponse> verifyPhoneOtp(
            @Valid @RequestBody OtpVerificationRequest request,
            @AuthenticationPrincipal RappiUserDetails userDetails
    ) {
        AuthResponse response = authenticationService.verifyPhoneOtp(
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
