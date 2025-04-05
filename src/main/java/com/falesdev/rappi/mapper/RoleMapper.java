package com.falesdev.rappi.mapper;

import com.falesdev.rappi.domain.dto.RoleDto;
import com.falesdev.rappi.domain.document.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    RoleDto toDto(Role role);
}
