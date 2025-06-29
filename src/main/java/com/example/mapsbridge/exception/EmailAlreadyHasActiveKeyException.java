package com.example.mapsbridge.exception;

/**
 * Exception thrown when an email already has an active API key.
 */
public class EmailAlreadyHasActiveKeyException extends RuntimeException {

    public EmailAlreadyHasActiveKeyException(String message) {
        super(message);
    }

    public EmailAlreadyHasActiveKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}