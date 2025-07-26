package com.example.mapsbridge.service.ratelimit;

import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.exception.rate.ChatIdRateLimitExceededException;
import com.example.mapsbridge.exception.rate.EmailRateLimitExceededException;
import com.example.mapsbridge.exception.rate.IpRateLimitExceededException;
import com.example.mapsbridge.exception.rate.RateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for rate limiting user sign-up requests by IP and email using Resilience4j.
 * Chat ID-based rate limiting is not supported for user sign-ups.
 */
@Slf4j
@Service
public class UserRateLimiterService extends AbstractRateLimiterService {

    private static final String IP_CONFIG_NAME = "ipConfig";
    private static final String EMAIL_CONFIG_NAME = "emailConfig";
    private static final String CHAT_ID_CONFIG_NAME = "chatIdConfig";

    @Autowired
    public UserRateLimiterService(RateLimiterRegistry rateLimiterRegistry) {
        super(rateLimiterRegistry);
    }

    /**
     * Check if a request from the current IP should be allowed.
     * Throws IpRateLimitExceededException if the rate limit is exceeded.
     * The IP address is retrieved from the LoggingContext.
     *
     * @throws IpRateLimitExceededException if the rate limit for this IP is exceeded
     */
    public void checkIpRateLimit() {
        String ipAddress = LoggingContext.getIpAddress();
        checkRateLimitForIp(ipAddress);
    }

    /**
     * Chat ID-based rate limiting is not supported for user sign-ups.
     * This method overrides the parent method to throw UnsupportedOperationException.
     *
     * @param chatId the chat ID
     * @throws UnsupportedOperationException always, as this operation is not supported
     */
    @Override
    public void checkRateLimitForChatId(String chatId) {
        throw new UnsupportedOperationException("Chat ID-based rate limiting is not supported for user sign-ups");
    }

    @Override
    protected String getIpConfigName() {
        return IP_CONFIG_NAME;
    }

    @Override
    protected String getEmailConfigName() {
        return EMAIL_CONFIG_NAME;
    }

    @Override
    protected String getChatIdConfigName() {
        return CHAT_ID_CONFIG_NAME;
    }

    @Override
    protected RateLimitExceededException createIpException(String ip) {
        return new IpRateLimitExceededException(ip);
    }

    @Override
    protected RateLimitExceededException createEmailException(String email) {
        return new EmailRateLimitExceededException(email);
    }

    @Override
    protected RateLimitExceededException createChatIdException(String chatId) {
        return new ChatIdRateLimitExceededException(chatId);
    }
}