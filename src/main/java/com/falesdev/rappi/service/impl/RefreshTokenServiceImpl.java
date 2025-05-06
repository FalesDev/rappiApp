package com.falesdev.rappi.service.impl;

import com.falesdev.rappi.domain.document.RefreshToken;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.domain.dto.response.AuthResponse;
import com.falesdev.rappi.exception.DocumentNotFoundException;
import com.falesdev.rappi.exception.InvalidRefreshTokenException;
import com.falesdev.rappi.repository.mongo.RefreshTokenRepository;
import com.falesdev.rappi.repository.mongo.UserRepository;
import com.falesdev.rappi.security.RappiUserDetails;
import com.falesdev.rappi.service.JwtService;
import com.falesdev.rappi.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(String userId) {
        refreshTokenRepository.deleteByUserId(userId);

        User user = userRepository.findById(userId).orElseThrow();
        UserDetails userDetails = new RappiUserDetails(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(jwtService.generateRefreshToken(userDetails))
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);

        return refreshTokenRepository.findByToken(refreshToken)
                .map(token -> {
                    refreshTokenRepository.delete(token);
                    refreshTokenRepository.deleteByUserId(token.getUserId());

                    User user = userRepository.findById(token.getUserId())
                            .orElseThrow(() -> new DocumentNotFoundException("User not found"));

                    UserDetails userDetails = new RappiUserDetails(user);
                    String newAccessToken = jwtService.generateAccessToken(userDetails);
                    String newRefreshToken = jwtService.generateRefreshToken(userDetails);

                    RefreshToken newToken = RefreshToken.builder()
                            .userId(user.getId())
                            .token(newRefreshToken)
                            .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpirationMs()))
                            .revoked(false)
                            .build();
                    refreshTokenRepository.save(newToken);

                    long expiresIn = jwtService.getJwtExpirationMs() / 1000;

                    return AuthResponse.builder()
                            .token(newAccessToken)
                            .refreshToken(newToken.getToken())
                            .expiresIn(expiresIn)
                            .build();
                })
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
    }

    @Override
    @Transactional
    public void validateRefreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (token.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token revoked");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }
    }
}
