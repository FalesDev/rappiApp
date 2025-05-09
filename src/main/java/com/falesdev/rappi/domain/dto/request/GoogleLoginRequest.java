package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public record GoogleLoginRequest (
        @NotBlank(message = "Token is required")
        String idToken
){
}
