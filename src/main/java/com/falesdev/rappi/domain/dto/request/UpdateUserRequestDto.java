package com.falesdev.rappi.domain.dto.request;

import com.falesdev.rappi.domain.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDto {

    @NotNull(message = "User ID is required")
    private String id;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 6, max = 20, message = "Password must be between {min} and {max} characters")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Role is required")
    private String roleId;

    @URL(protocol = "https", message = "Must be a valid HTTPS URL")
    private String imageURL;
}
