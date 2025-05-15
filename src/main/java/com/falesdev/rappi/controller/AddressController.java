package com.falesdev.rappi.controller;

import com.falesdev.rappi.domain.document.Address;
import com.falesdev.rappi.domain.dto.request.AddressRequest;
import com.falesdev.rappi.security.RappiUserDetails;
import com.falesdev.rappi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping(path = "/api/v1/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Set<Address>> getAddresses(@AuthenticationPrincipal RappiUserDetails userDetails) {
        return ResponseEntity.ok(userService.getAddresses(userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<Void> addAddress(
            @RequestBody AddressRequest addressRequest,
            @AuthenticationPrincipal RappiUserDetails userDetails) {
        userService.addAddress(userDetails.getId(), addressRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{addressLine}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable String addressLine,
            @AuthenticationPrincipal RappiUserDetails userDetails) {
        userService.deleteAddress(userDetails.getId(), addressLine);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressLine}/select")
    public ResponseEntity<Void> selectAddress(
            @PathVariable String addressLine,
            @AuthenticationPrincipal RappiUserDetails userDetails) {
        userService.selectAddress(userDetails.getId(), addressLine);
        return ResponseEntity.ok().build();
    }
}
