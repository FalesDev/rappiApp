package com.falesdev.rappi.service.impl;

import com.falesdev.rappi.domain.RegisterType;
import com.falesdev.rappi.domain.document.Address;
import com.falesdev.rappi.domain.dto.UserDto;
import com.falesdev.rappi.domain.dto.request.AddressRequest;
import com.falesdev.rappi.domain.dto.request.CreateUserRequestDto;
import com.falesdev.rappi.domain.dto.request.UpdateUserRequestDto;
import com.falesdev.rappi.domain.document.Role;
import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.exception.AddressAlreadyExistsException;
import com.falesdev.rappi.exception.AddressNotExistsException;
import com.falesdev.rappi.exception.DocumentNotFoundException;
import com.falesdev.rappi.mapper.UserMapper;
import com.falesdev.rappi.repository.UserRepository;
import com.falesdev.rappi.service.RoleService;
import com.falesdev.rappi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final MongoTemplate mongoTemplate;

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
                .orElseThrow(() -> new DocumentNotFoundException("User not found"));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequestDto userRequestDto) {
        if (userRepository.existsByEmailIgnoreCase(userRequestDto.getEmail())){
            throw new IllegalArgumentException("User already exists with email: " + userRequestDto.getFirstName());
        }

        User newUser = userMapper.toCreateUser(userRequestDto);
        newUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        newUser.setRole(roleService.getRoleById(userRequestDto.getRoleId()));
        newUser.setRegisterType(RegisterType.LOCAL);

        User savedUser = userRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(String id, UpdateUserRequestDto updateUserRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(()-> new DocumentNotFoundException("User does not exist"));
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
                .orElseThrow(()-> new DocumentNotFoundException("User does not exist"));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Address> getAddresses(String userId){
        User user = getCurrentUser(userId);
        return user.getAddresses();
    }

    @Override
    @Transactional
    public void addAddress(String userId, AddressRequest addressRequest) {
        User user = getCurrentUser(userId);

        boolean exists = user.getAddresses().stream()
                .anyMatch(a -> a.getAddressLine().equals(addressRequest.getAddressLine()));

        if (exists) {
            throw new AddressAlreadyExistsException("The address already exists for the user");
        }

        boolean isFirstAddress = user.getAddresses().isEmpty();
        Address newAddress = toAddress(addressRequest);
        newAddress.setSelected(isFirstAddress || addressRequest.isSelected());

        if (newAddress.isSelected() && !isFirstAddress) {
            deselectAllAddresses(user);
        }

        user.getAddresses().add(newAddress);
        user.setAddresses(new HashSet<>(user.getAddresses()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAddress(String userId, String addressLine) {
        User user = getCurrentUser(userId);

        Address toDelete = user.getAddresses().stream()
                .filter(a -> a.getAddressLine().equals(addressLine))
                .findFirst()
                .orElseThrow(() -> new AddressNotExistsException("Address does not exist for this user"));

        if (user.getAddresses().size() == 1) {
            throw new IllegalStateException("Cannot delete last address");
        }

        user.getAddresses().remove(toDelete);

        if (toDelete.isSelected()) {
            selectFirstAddress(user);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void selectAddress(String userId, String addressLine) {
        Query checkQuery = Query.query(
                Criteria.where("_id").is(userId)
                        .and("addresses.addressLine").is(addressLine)
        );
        if (!mongoTemplate.exists(checkQuery, User.class)) {
            throw new AddressNotExistsException("Address does not exist");
        }

        Query query = Query.query(Criteria.where("_id").is(userId));
        Update update = new Update()
                .set("addresses.$[].isSelected", false)
                .set("addresses.$[elem].isSelected", true)
                .filterArray(Criteria.where("elem.addressLine").is(addressLine));

        mongoTemplate.updateFirst(query, update, User.class);
    }

    private User getCurrentUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DocumentNotFoundException("User does not exist"));
    }

    private void deselectAllAddresses(User user) {
        user.getAddresses().forEach(a -> a.setSelected(false));
    }

    private void selectFirstAddress(User user) {
        user.getAddresses().iterator().next().setSelected(true);
    }

    private Address toAddress(AddressRequest addressRequest) {
        return  Address.builder()
                .addressLine(addressRequest.getAddressLine())
                .latitude(addressRequest.getLatitude())
                .longitude(addressRequest.getLongitude())
                .tag(addressRequest.getTag())
                .buildingName(addressRequest.getBuildingName())
                .unitNumber(addressRequest.getUnitNumber())
                .isSelected(addressRequest.isSelected())
                .build();
    }
}
