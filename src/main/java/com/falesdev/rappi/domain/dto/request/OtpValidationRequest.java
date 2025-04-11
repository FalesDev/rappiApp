package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpValidationRequest(
        @NotBlank
        String identifier,
        @NotBlank
        @Size(min = 6, max = 6)
        String code
) {
}
