package com.falesdev.rappi.service.impl;

import com.falesdev.rappi.domain.RegisterType;
import com.falesdev.rappi.domain.OtpType;
import com.falesdev.rappi.domain.dto.AuthResponse;
import com.falesdev.rappi.domain.dto.AuthUser;
import com.falesdev.rappi.domain.redis.Otp;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.exception.BadRequestException;
import com.falesdev.rappi.exception.DocumentNotFoundException;
import com.falesdev.rappi.exception.OtpInvalidException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenValidator googleTokenValidator;
    private final OAuth2UserManagementService oAuth2UserManagementService;
    private final RefreshTokenService refreshTokenService;
    private final RoleMapper roleMapper;

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Override
    public void sendPhoneOtp(String phone) {
        validateUserExistsByPhone(phone);
        String otpCode = generateOtp();
        otpRepository.save(new Otp(phone, otpCode, OtpType.LOGIN_PHONE,phone));
        smsService.sendOtpSms(phone, otpCode);
    }

    @Override
    @Transactional
    public AuthResponse validatePhoneOtp(String phone, String otpCode) {
        User user = validateOtpAndGetUser(phone, otpCode);
        return generateAuthResponse(user);
    }

    @Override
    public void sendEmailOtp(String email) {
        validateUserExistsByEmail(email);
        String otpCode = generateOtp();
        otpRepository.save(new Otp(email, otpCode, OtpType.LOGIN_EMAIL,email));
        emailService.sendOtpEmail(email, otpCode);
    }


    @Override
    @Transactional
    public AuthResponse validateEmailOtp(String email, String otpCode) {
        User user = validateOtpAndGetUser(email, otpCode);
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

    /*@Override
    @Transactional
    public AuthResponse registerWithPhone(String phone) {
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
        String token = jwtService.generateAccessToken(userDetails);
        long expiresIn = jwtService.getExpirationTime(token) / 1000;

        return AuthResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }*/

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
    public void sendGooglePhoneVerificationOtp(String email, String phone) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DocumentNotFoundException("User not found"));

        if (user.getRegisterType() != RegisterType.GOOGLE) {
            throw new BadRequestException("Only Google users can verify phone");
        }

        String otpCode = generateOtp();
        otpRepository.save(new Otp(email, otpCode, OtpType.REGISTER_GOOGLE,phone));

        smsService.sendOtpSms(phone, otpCode);
    }

    @Override
    @Transactional
    public AuthResponse verifyGooglePhoneOtp(String email, String otpCode) {
        Otp otp = otpRepository.findById(email)
                .orElseThrow(() -> new OtpInvalidException("OTP invalid or expired"));

        if (!otp.getCode().equals(otpCode) || otp.getType() != OtpType.REGISTER_GOOGLE) {
            throw new OtpInvalidException("OTP invalid");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DocumentNotFoundException("User not found"));

        user.setPhone(otp.getTarget());
        user.setPhoneVerified(true);
        userRepository.save(user);

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

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(999999));
    }

    private void validateUserExistsByPhone(String phone) {
        if (!userRepository.existsByPhone(phone)) {
            throw new DocumentNotFoundException("User not found");
        }
    }

    private void validateUserExistsByEmail(String email) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            throw new DocumentNotFoundException("User not found");
        }
    }

    private User validateOtpAndGetUser(String identifier, String otpCode) {
        Otp otp = otpRepository.findById(identifier)
                .orElseThrow(() -> new OtpInvalidException("OTP invalid or expired"));

        if (!otp.getCode().equals(otpCode)) {
            throw new OtpInvalidException("OTP incorrect");
        }

        return switch (otp.getType()) {
            case LOGIN_EMAIL -> userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new DocumentNotFoundException("User not found"));
            case LOGIN_PHONE -> userRepository.findByPhone(identifier)
                    .orElseThrow(() -> new DocumentNotFoundException("User not found"));
            default -> throw new OtpInvalidException("OTP is not for login");
        };
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
                .build();
    }
}
