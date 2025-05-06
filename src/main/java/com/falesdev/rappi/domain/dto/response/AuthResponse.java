package com.falesdev.rappi.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String refreshToken;
    private long expiresIn;

    @Builder.Default
    private boolean requiresPhone = false;
}
