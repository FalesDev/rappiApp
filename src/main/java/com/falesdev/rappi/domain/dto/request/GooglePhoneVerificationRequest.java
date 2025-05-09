package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GooglePhoneVerificationRequest(
        @NotBlank(message = "User ID is required")
        String userId,

        @NotBlank
        @Pattern(regexp = "^\\+[0-9]{6,14}[0-9]$", message = "Phone number must be valid")
        String phone
) {
}
