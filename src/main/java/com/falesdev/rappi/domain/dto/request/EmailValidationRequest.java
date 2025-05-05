package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailValidationRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank
        @Size(min = 6, max = 6)
        String code
) {
}
