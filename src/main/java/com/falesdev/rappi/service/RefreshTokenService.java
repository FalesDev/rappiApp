package com.falesdev.rappi.service;

import com.falesdev.rappi.domain.document.RefreshToken;
import com.falesdev.rappi.domain.dto.response.AuthResponse;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String userId);
    AuthResponse refreshAccessToken(String refreshToken);
    void validateRefreshToken(String refreshToken);
}
