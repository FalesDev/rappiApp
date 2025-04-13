package com.falesdev.rappi.service.impl;

import com.falesdev.rappi.domain.LoginType;
import com.falesdev.rappi.domain.dto.UserDto;
import com.falesdev.rappi.domain.dto.request.CreateUserRequestDto;
import com.falesdev.rappi.domain.dto.request.UpdateUserRequestDto;
import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.exception.DocumentNotFoundException;
import com.falesdev.rappi.mapper.UserMapper;
import com.falesdev.rappi.repository.mongo.UserRepository;
import com.falesdev.rappi.service.RoleService;
import com.falesdev.rappi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleService roleService;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequestDto userRequestDto) {
        if (userRepository.existsByEmailIgnoreCase(userRequestDto.getEmail())){
            throw new IllegalArgumentException("User already exists with email: " + userRequestDto.getName());
        }

        User newUser = userMapper.toCreateUser(userRequestDto);
        newUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        newUser.setRole(roleService.getRoleById(userRequestDto.getRoleId()));
        newUser.setLoginType(LoginType.LOCAL);

        User savedUser = userRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(String id, UpdateUserRequestDto updateUserRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new DocumentNotFoundException("User does not exist with id "+id));
        userMapper.updateFromDto(updateUserRequestDto, existingUser);

        if(updateUserRequestDto.getRoleId() != null){
            Role validRole = roleService.getRoleById(updateUserRequestDto.getRoleId());
            existingUser.setRole(validRole);
        }

        if (updateUserRequestDto.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(updateUserRequestDto.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new DocumentNotFoundException("User does not exist with ID "+id));
        userRepository.delete(user);
    }
}
