package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+[0-9]{6,14}[0-9]$", message = "Phone number must be valid")
        String phone,

        @NotBlank(message = "FirstName is required")
        String firstName,

        @NotBlank(message = "LastName is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email
) {
}
