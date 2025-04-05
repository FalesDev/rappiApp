package com.falesdev.rappi.domain.dto;

import com.falesdev.rappi.domain.LoginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private String id;
    private String email;
    private String password;
    private String name;
    private RoleDto role;
    private String imageURL;
    private LoginType loginType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
