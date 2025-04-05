package com.falesdev.rappi.service;

import com.falesdev.rappi.domain.dto.UserDto;
import com.falesdev.rappi.domain.dto.request.CreateUserRequestDto;
import com.falesdev.rappi.domain.dto.request.UpdateUserRequestDto;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDto> getAllUsers();
    UserDto getUserById(String id);
    UserDto createUser(CreateUserRequestDto userDto);
    UserDto updateUser(String id, UpdateUserRequestDto userDto);
    void deleteUser(String id);
}
