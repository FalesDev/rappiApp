package com.falesdev.rappi.service.impl;

import com.falesdev.rappi.domain.RegisterType;
import com.falesdev.rappi.domain.OtpType;
import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.dto.request.RegisterRequest;
import com.falesdev.rappi.domain.dto.response.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.dto.response.PhoneRegisterResponse;
import com.falesdev.rappi.domain.redis.Otp;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.exception.*;
import com.falesdev.rappi.mapper.RoleMapper;
import com.falesdev.rappi.repository.redis.OtpRepository;
import com.falesdev.rappi.repository.mongo.RoleRepository;
import com.falesdev.rappi.repository.mongo.UserRepository;
import com.falesdev.rappi.security.RappiUserDetails;
import com.falesdev.rappi.security.auth.GoogleTokenValidator;
import com.falesdev.rappi.security.service.OAuth2UserManagementService;
import com.falesdev.rappi.service.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GoogleTokenValidator googleTokenValidator;
    private final OAuth2UserManagementService oAuth2UserManagementService;
    private final RefreshTokenService refreshTokenService;
    private final RoleMapper roleMapper;

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Override
    public void sendLoginPhoneOtp(String phone) {
        String otpCode = generateOtp();
        otpRepository.save(new Otp(otpCode, OtpType.LOGIN_PHONE,phone));
        smsService.sendOtpSms(phone, otpCode);
    }

    @Override
    @Transactional
    public AuthResponse validateLoginPhoneOtp(String phone, String otpCode) {
        validateGlobalOtp(otpCode, OtpType.LOGIN_PHONE, phone);

        if (!userRepository.existsByPhone(phone)) {
            throw new PhoneNotExistsException("Phone is not registered");
        }

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new DocumentNotFoundException("User not found"));

        return generateAuthResponse(user);
    }

    @Override
    public void sendLoginEmailOtp(String email) {
        validateEmailExists(email);
        String otpCode = generateOtp();
        otpRepository.save(new Otp(otpCode, OtpType.LOGIN_EMAIL,email));
        emailService.sendOtpEmail(email, otpCode);
    }

    @Override
    @Transactional
    public AuthResponse validateLoginEmailOtp(String email, String otpCode) {
        validateGlobalOtp(otpCode, OtpType.LOGIN_EMAIL, email);

        if (!userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailNotExistsException("Email is not registered");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DocumentNotFoundException("User not found"));
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

    @Override
    public void sendRegisterPhoneOtp(String phone) {
        String otpCode = generateOtp();
        otpRepository.save(new Otp(otpCode, OtpType.REGISTER_PHONE,phone));
        smsService.sendOtpSms(phone, otpCode);
    }

    @Override
    @Transactional
    public PhoneRegisterResponse validateRegisterPhoneOtp(String phone, String otpCode) {
        validateGlobalOtp(otpCode, OtpType.REGISTER_PHONE, phone);
        Boolean isRegistered = userRepository.existsByPhone(phone);

        return PhoneRegisterResponse.builder()
                .phone(phone)
                .isRegistered(isRegistered)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse registerWithPhone(RegisterRequest registerRequest) {
        if (userRepository.existsByEmailIgnoreCase(registerRequest.email())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        if(userRepository.existsByPhone(registerRequest.phone())) {
            throw new PhoneAlreadyExistsException("Phone is already registered");
        }

        Role userRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new DocumentNotFoundException("Role CUSTOMER not found"));

        User newUser = User.builder()
                .email(registerRequest.email())
                .password(null)
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .phone(registerRequest.phone())
                .phoneVerified(true)
                .role(userRole)
                .registerType(RegisterType.PHONE)
                .build();

        userRepository.save(newUser);

        return generateAuthResponse(newUser);
    }

    @Override
    @Transactional
    public AuthResponse registerWithGoogle(String idToken) {
        GoogleIdToken.Payload payload = googleTokenValidator.validate(idToken);

        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String picture = (String) payload.get("picture");

        User user = oAuth2UserManagementService.createOrUpdateOAuth2User(
                email,
                firstName,
                lastName,
                picture,
                "GOOGLE"
        );

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", payload.getSubject());
        attributes.put("email", email);
        attributes.put("given_name", firstName);
        attributes.put("family_name", lastName);
        attributes.put("picture", picture);

        RappiUserDetails userDetails = new RappiUserDetails(user, attributes);
        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        long expiresIn = jwtService.getExpirationTime(accessToken) / 1000;

        boolean requiresPhone = user.getPhone() == null || !user.isPhoneVerified();

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .requiresPhone(requiresPhone)
                .build();
    }

    @Override
    public void sendRegisterGooglePhoneOtp(String email, String phone) {
        String otpCode = generateOtp();
        otpRepository.save(new Otp(otpCode, OtpType.REGISTER_GOOGLE,phone));
        smsService.sendOtpSms(phone, otpCode);
    }

    @Override
    @Transactional
    public AuthResponse verifyRegisterGooglePhoneOtp(String email, String phone, String otpCode) {
        validateGlobalOtp(otpCode, OtpType.REGISTER_GOOGLE, phone);

        if (userRepository.existsByPhone(phone)) {
            throw new PhoneAlreadyExistsException("Phone is already registered");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DocumentNotFoundException("User not found"));

        user.setPhone(phone);
        user.setPhoneVerified(true);
        userRepository.save(user);

        return generateAuthResponse(user);
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
    }

    private void validateEmailExists(String email) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailNotExistsException("Email is not registered");
        }
    }

    private void validateGlobalOtp(String otpCode, OtpType otpType, String target) {
        Otp otp = otpRepository.findByTypeAndTarget(otpType, target)
                .orElseThrow(() -> new OtpInvalidException("OTP mismatch or expired"));

        if (!otp.getCode().equals(otpCode)) {
            throw new OtpInvalidException("OTP code incorrect");
        }

        otpRepository.deleteById(otp.getKey());
    }

    private AuthResponse generateAuthResponse(User user) {
        RappiUserDetails userDetails = new RappiUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        long expiresIn = jwtService.getExpirationTime(accessToken) / 1000;

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .requiresPhone(false)
                .build();
    }
}
