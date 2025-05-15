package com.falesdev.rappi.exception;

public class AddressNotExistsException extends RuntimeException {
    public AddressNotExistsException(String message) {
        super(message);
    }
}
