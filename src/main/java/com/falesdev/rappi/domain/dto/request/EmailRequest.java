package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email
) {
}