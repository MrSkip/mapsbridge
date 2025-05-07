package com.example.mapsbridge.exception;

/**
 * Exception thrown when coordinates cannot be extracted from a URL.
 */
public class CoordinateExtractionException extends RuntimeException {
    
    public CoordinateExtractionException(String message) {
        super(message);
    }
    
    public CoordinateExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}