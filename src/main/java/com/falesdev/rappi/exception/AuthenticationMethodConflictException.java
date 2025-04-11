package com.falesdev.rappi.exception;

public class AuthenticationMethodConflictException extends RuntimeException {
    public AuthenticationMethodConflictException(String message) {
        super(message);
    }
}
