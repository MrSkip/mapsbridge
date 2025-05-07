package com.example.mapsbridge.exception;

/**
 * Exception thrown when the input is neither valid coordinates nor a valid URL.
 */
public class InvalidInputException extends RuntimeException {
    
    public InvalidInputException(String message) {
        super(message);
    }
    
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}