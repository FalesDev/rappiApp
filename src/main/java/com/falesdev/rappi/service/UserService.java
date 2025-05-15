package com.falesdev.rappi.service;

import com.falesdev.rappi.domain.document.Address;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.domain.dto.UserDto;
import com.falesdev.rappi.domain.dto.request.AddressRequest;
import com.falesdev.rappi.domain.dto.request.CreateUserRequestDto;
import com.falesdev.rappi.domain.dto.request.UpdateUserRequestDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserService {

    List<UserDto> getAllUsers();
    UserDto getUserById(String id);
    UserDto createUser(CreateUserRequestDto userDto);
    UserDto updateUser(String id, UpdateUserRequestDto userDto);
    void deleteUser(String id);
    Set<Address> getAddresses(String userId);
    void addAddress(String userId, AddressRequest addressRequest);
    void deleteAddress(String userId, String addressLine);
    void selectAddress(String userId, String addressLine);
}
