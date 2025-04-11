package com.falesdev.rappi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class OtpInvalidException extends RuntimeException{
    public OtpInvalidException(String message) {
        super(message);
    }
}
