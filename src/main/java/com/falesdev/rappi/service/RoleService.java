package com.falesdev.rappi.service;

import com.falesdev.rappi.domain.dto.RoleDto;
import com.falesdev.rappi.domain.document.Role;

import java.util.List;

public interface RoleService {

    List<RoleDto> getAllRoles();
    Role getRoleById(String id);
}
