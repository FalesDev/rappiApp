package com.falesdev.rappi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthUser {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private RoleDto role;
}
