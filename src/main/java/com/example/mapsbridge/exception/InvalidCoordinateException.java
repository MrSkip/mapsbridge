package com.example.mapsbridge.exception;

/**
 * Exception thrown when coordinates are invalid or in an incorrect format.
 */
public class InvalidCoordinateException extends RuntimeException {
    
    public InvalidCoordinateException(String message) {
        super(message);
    }
    
    public InvalidCoordinateException(String message, Throwable cause) {
        super(message, cause);
    }
}