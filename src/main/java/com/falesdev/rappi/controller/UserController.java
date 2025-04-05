package com.falesdev.rappi.controller;

import com.falesdev.rappi.domain.dto.UserDto;
import com.falesdev.rappi.domain.dto.request.CreateUserRequestDto;
import com.falesdev.rappi.domain.dto.request.UpdateUserRequestDto;
import com.falesdev.rappi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable String id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequestDto createUserRequestDto){
        return new ResponseEntity<>(userService.createUser(createUserRequestDto), HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String id, @Valid @RequestBody UpdateUserRequestDto updateUserRequestDto){
        return ResponseEntity.ok(userService.updateUser(id,updateUserRequestDto));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
