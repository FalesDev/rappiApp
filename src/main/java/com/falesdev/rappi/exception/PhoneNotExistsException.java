package com.falesdev.rappi.exception;

public class PhoneNotExistsException extends RuntimeException {
    public PhoneNotExistsException(String message) {
        super(message);
    }
}
