package com.example.mapsbridge.exception.rate;

/**
 * Exception thrown when a rate limit is exceeded.
 * This is the base class for all rate limiting exceptions.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Rate limit exceeded");
    }

    public RateLimitExceededException(String message) {
        super(message);
    }
    
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}