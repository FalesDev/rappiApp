package com.falesdev.rappi.exception;

public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException(String message) {
        super(message);
    }
}
