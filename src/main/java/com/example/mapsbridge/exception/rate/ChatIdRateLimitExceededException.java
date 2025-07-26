package com.example.mapsbridge.exception.rate;

import lombok.Getter;

/**
 * Exception thrown when the rate limit for a chat ID is exceeded.
 */
@Getter
public class ChatIdRateLimitExceededException extends RateLimitExceededException {

    /**
     * -- GETTER --
     * Get the chat ID that exceeded the rate limit.
     *
     * @return the chat ID
     */
    private final String chatId;

    public ChatIdRateLimitExceededException(String chatId) {
        super("Rate limit exceeded for chat ID: " + chatId);
        this.chatId = chatId;
    }

    public ChatIdRateLimitExceededException(String chatId, Throwable cause) {
        super("Rate limit exceeded for chat ID: " + chatId, cause);
        this.chatId = chatId;
    }
}