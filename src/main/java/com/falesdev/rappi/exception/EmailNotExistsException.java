package com.falesdev.rappi.exception;

public class EmailNotExistsException extends RuntimeException {
    public EmailNotExistsException(String message) {
        super(message);
    }
}
