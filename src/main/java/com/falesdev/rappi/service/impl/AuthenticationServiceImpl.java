package com.falesdev.rappi.service.impl;

import com.falesdev.rappi.domain.dto.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.GoogleLoginRequest;
import com.falesdev.rappi.domain.dto.RoleDto;
import com.falesdev.rappi.domain.dto.request.SignupRequest;
import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.exception.DocumentNotFoundException;
import com.falesdev.rappi.exception.EmailAlreadyExistsException;
import com.falesdev.rappi.repository.RoleRepository;
import com.falesdev.rappi.repository.UserRepository;
import com.falesdev.rappi.security.RappiUserDetails;
import com.falesdev.rappi.security.auth.GoogleTokenValidator;
import com.falesdev.rappi.security.service.OAuth2UserManagementService;
import com.falesdev.rappi.service.AuthenticationService;
import com.falesdev.rappi.service.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenValidator googleTokenValidator;
    private final OAuth2UserManagementService oAuth2UserManagementService;

    @Override
    public AuthResponse authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email,password)
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String token = jwtService.generateToken(userDetails);
        long expiresIn = jwtService.getExpirationTime(token);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }

    @Override
    public AuthResponse register(SignupRequest signupRequest) {
        if (userRepository.existsByEmailIgnoreCase(signupRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        Role userRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new DocumentNotFoundException("Role CUSTOMER not found"));

        User newUser = User.builder()
                .name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .role(userRole)
                .build();

        userRepository.save(newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getEmail());
        String token = jwtService.generateToken(userDetails);
        long expiresIn = jwtService.getExpirationTime(token);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }

    @Override
    public UserDetails validateToken(String token) {
        try {
            final Claims claims = jwtService.parseClaims(token);
            final String username = claims.getSubject();

            return userDetailsService.loadUserByUsername(username);
        } catch (ExpiredJwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired", ex);
        } catch (JwtException | UsernameNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication error", ex);
        }
    }

    @Override
    public AuthUser getUserProfile(Authentication authentication) {
        RappiUserDetails userDetails = (RappiUserDetails) authentication.getPrincipal();

        Role role = userDetails.getUser().getRole();
        RoleDto roleDto = new RoleDto(role.getId(), role.getName());

        return new AuthUser(
                userDetails.getId(),
                userDetails.getUser().getName(),
                userDetails.getUsername(),
                roleDto
        );
    }

    @Override
    public AuthResponse authenticateWithGoogle(String idToken) {
        GoogleIdToken.Payload payload = googleTokenValidator.validate(idToken);

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = oAuth2UserManagementService.createOrUpdateOAuth2User(
                email,
                name,
                picture,
                "GOOGLE"
        );

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", payload.getSubject());
        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("picture", picture);

        RappiUserDetails userDetails = new RappiUserDetails(user, attributes);

        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime(jwtToken))
                .build();
    }
}
