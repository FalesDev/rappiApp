package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OtpRequest(
        @NotBlank
        String identifier
) {
}
