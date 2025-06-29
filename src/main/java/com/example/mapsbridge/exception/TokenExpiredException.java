package com.example.mapsbridge.exception;

/**
 * Exception thrown when a token has expired.
 */
public class TokenExpiredException extends InvalidTokenException {

    public TokenExpiredException(String message) {
        super(message);
    }
}