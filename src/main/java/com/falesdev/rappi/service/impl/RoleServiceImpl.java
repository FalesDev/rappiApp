package com.falesdev.rappi.service.impl;

import com.falesdev.rappi.domain.dto.RoleDto;
import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.exception.DocumentNotFoundException;
import com.falesdev.rappi.mapper.RoleMapper;
import com.falesdev.rappi.repository.mongo.RoleRepository;
import com.falesdev.rappi.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {


    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Role not found with id" + id));
    }
}
