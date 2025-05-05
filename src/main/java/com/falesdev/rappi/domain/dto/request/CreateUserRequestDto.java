package com.falesdev.rappi.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDto {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 6, max = 20, message = "Password must be between {min} and {max} characters")
    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "FirstName is required")
    private String firstName;

    @NotBlank(message = "LastName is required")
    private String lastName;

    private String nickname;

    @NotBlank(message = "DNI is mandatory")
    @Pattern(
            regexp = "^[1-9]\\d{7}$",
            message = "DNI must have 8 numeric digits (1-9)"
    )
    private String dni;

    @Pattern(regexp = "^\\+[0-9]{6,14}[0-9]$", message = "Phone number must be valid")
    private String phone;

    private LocalDateTime birthday;

    private boolean phoneVerified;

    @NotNull(message = "Role is required")
    private String roleId;

    @URL(protocol = "https", message = "Must be a valid HTTPS URL")
    private String imageURL;

    @Builder.Default
    private Set<String> addresses = new HashSet<>();
}
