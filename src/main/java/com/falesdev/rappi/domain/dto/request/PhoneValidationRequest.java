package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PhoneValidationRequest(
        @NotBlank
        @Pattern(regexp = "^\\+[0-9]{6,14}[0-9]$", message = "Phone number must be valid")
        String phone,
        @NotBlank
        @Size(min = 6, max = 6)
        String code
) {
}
