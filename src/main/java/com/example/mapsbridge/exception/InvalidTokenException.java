package com.example.mapsbridge.exception;

/**
 * Exception thrown when a token is invalid or not found.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
    public InvalidTokenException() {
        super("Invalid token");
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}