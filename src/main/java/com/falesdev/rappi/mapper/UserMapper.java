package com.falesdev.rappi.mapper;

import com.falesdev.rappi.domain.dto.UserDto;
import com.falesdev.rappi.domain.dto.request.CreateUserRequestDto;
import com.falesdev.rappi.domain.dto.request.UpdateUserRequestDto;
import com.falesdev.rappi.domain.document.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "role", source = "role")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toCreateUser(CreateUserRequestDto dto);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(UpdateUserRequestDto dto, @MappingTarget User user);
}
