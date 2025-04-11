package com.falesdev.rappi.domain.dto.request;

import com.falesdev.rappi.domain.LoginType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

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

    @NotBlank(message = "Name is required")
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be valid")
    private String phone;

    private boolean phoneVerified;

    @NotNull(message = "Role is required")
    private String roleId;

    @URL(protocol = "https", message = "Must be a valid HTTPS URL")
    private String imageURL;
}
