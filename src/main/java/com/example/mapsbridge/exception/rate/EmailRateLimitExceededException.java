package com.example.mapsbridge.exception.rate;

import lombok.Getter;

/**
 * Exception thrown when the rate limit for an email address is exceeded.
 */
@Getter
public class EmailRateLimitExceededException extends RateLimitExceededException {

    /**
     * -- GETTER --
     *  Get the email address that exceeded the rate limit.
     *
     * @return the email address
     */
    private final String email;
    
    public EmailRateLimitExceededException(String email) {
        super("Rate limit exceeded for email: " + email);
        this.email = email;
    }
    
    public EmailRateLimitExceededException(String email, Throwable cause) {
        super("Rate limit exceeded for email: " + email, cause);
        this.email = email;
    }

}