package com.falesdev.rappi.controller;

import com.falesdev.rappi.domain.dto.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.GoogleLoginRequest;
import com.falesdev.rappi.domain.dto.request.LoginRequest;
import com.falesdev.rappi.domain.dto.request.SignupRequest;
import com.falesdev.rappi.service.AuthenticationService;
import com.falesdev.rappi.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUser> getUserProfile(Authentication authentication) {
        return ResponseEntity.ok(authenticationService.getUserProfile(authentication));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authenticationService.register(signupRequest));
    }

    //Esto mas adelante a eliminar porque el token lo rederigiras al frontend
    @GetMapping("/oauth-success")
    public ResponseEntity<AuthResponse> oauthSuccess(@RequestParam String token) {
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationTime(token))
                .build());
    }

    //Recepcionas el token que te envian el front mobile
    @PostMapping("/google-login")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticateWithGoogle(request.getIdToken()));
    }
}
