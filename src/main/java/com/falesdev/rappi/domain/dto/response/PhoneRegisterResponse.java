package com.falesdev.rappi.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhoneRegisterResponse {

    private String phone;
    private Boolean isRegistered;
}
