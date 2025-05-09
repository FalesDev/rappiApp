package com.falesdev.rappi.service.impl;

import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.dto.request.RegisterRequest;
import com.falesdev.rappi.domain.dto.response.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.response.EmailLoginResponse;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.exception.*;
import com.falesdev.rappi.mapper.RoleMapper;
import com.falesdev.rappi.repository.RoleRepository;
import com.falesdev.rappi.repository.UserRepository;
import com.falesdev.rappi.security.RappiUserDetails;
import com.falesdev.rappi.security.auth.FirebaseTokenValidator;
import com.falesdev.rappi.security.auth.GoogleTokenValidator;
import com.falesdev.rappi.security.service.OAuth2UserManagementService;
import com.falesdev.rappi.service.*;
import com.google.firebase.auth.FirebaseToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OAuth2UserManagementService oAuth2UserManagementService;
    private final RefreshTokenService refreshTokenService;
    private final RoleMapper roleMapper;
    private final FirebaseTokenValidator firebaseTokenValidator;

    @Override
    @Transactional
    public AuthResponse verifyOtp(String fireBaseToken) {
        String phoneNumber = firebaseTokenValidator.getPhoneNumberFromToken(fireBaseToken);

        User user = userRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new PhoneNotExistsException("Phone is not registered"));

        return generateAuthResponse(user);
    }

    @Override
    public EmailLoginResponse verifyEmailLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotExistsException("Email is not registered"));

        return EmailLoginResponse.builder()
                .phone(user.getPhone())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateRegistration(request);

        Role role = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new DocumentNotFoundException("Default role not found"));

        User newUser = User.builder()
                .phone(request.phone())
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneVerified(true)
                .role(role)
                .build();

        userRepository.save(newUser);
        return generateAuthResponse(newUser);
    }

    @Override
    @Transactional
    public AuthResponse handleGoogleAuth(String fireBaseToken) {
        FirebaseToken firebaseToken = firebaseTokenValidator.validate(fireBaseToken);

        String email = firebaseToken.getEmail();
        String firstName = (String) firebaseToken.getClaims().get("given_name");
        String lastName = (String) firebaseToken.getClaims().get("family_name");
        String picture = (String) firebaseToken.getClaims().get("picture");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUnverifiedGoogleUser(email, firstName, lastName, picture));

        if (!user.isPhoneVerified()) {
            RappiUserDetails userDetails = new RappiUserDetails(user);
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
            long expiresIn = jwtService.getExpirationTime(accessToken) / 1000;

            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(expiresIn)
                    .requiresPhoneVerification(true)
                    .tempUserId(user.getId())
                    .build();
        }

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse verifyPhoneForGoogleUser(String tempUserId, String phone) {

        User user = userRepository.findById(tempUserId)
                .orElseThrow(() -> new DocumentNotFoundException("User not found"));

        if (userRepository.existsByPhone(phone)) {
            throw new PhoneAlreadyExistsException("Phone already registered");
        }

        user.setPhone(phone);
        user.setPhoneVerified(true);
        userRepository.save(user);

        return generateAuthResponse(user);
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
    @Transactional(readOnly = true)
    public AuthUser getUserProfile(RappiUserDetails userDetails) {
        User user = userDetails.getUser();
        return new AuthUser(
                userDetails.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                roleMapper.toDto(user.getRole())
        );
    }

    private User createUnverifiedGoogleUser(String email, String firstName, String lastName, String picture) {
        return oAuth2UserManagementService.createOrUpdateOAuth2User(
                email,
                firstName,
                lastName,
                picture,
                "GOOGLE"
        );
    }

    private void validateRegistration(RegisterRequest request) {
        if (userRepository.existsByPhone(request.phone())) {
            throw new PhoneAlreadyExistsException("Phone already registered");
        }

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        RappiUserDetails userDetails = new RappiUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        long expiresIn = jwtService.getExpirationTime(accessToken) / 1000;
        boolean requiresPhoneVerification = user.getPhone() == null && !user.isPhoneVerified();

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .requiresPhoneVerification(requiresPhoneVerification)
                .build();
    }
}
