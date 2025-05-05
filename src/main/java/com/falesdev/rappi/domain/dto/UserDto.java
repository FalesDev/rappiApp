package com.falesdev.rappi.domain.dto;

import com.falesdev.rappi.domain.RegisterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private String id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String nickname;
    private String dni;
    private String phone;
    private LocalDateTime birthday;
    private boolean phoneVerified;
    private RoleDto role;
    private String imageURL;
    private RegisterType registerType;
    private Set<String> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
