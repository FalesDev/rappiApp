package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerificationRequest(
        @NotBlank
        @Size(min = 6, max = 6)
        String otp
) {
}
