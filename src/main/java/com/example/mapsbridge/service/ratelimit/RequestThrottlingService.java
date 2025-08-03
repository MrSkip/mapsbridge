package com.example.mapsbridge.service.ratelimit;

import com.example.mapsbridge.exception.rate.RateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for throttling requests by IP address using Resilience4j.
 * Uses a shared configuration for all IP-based throttling.
 */
@Slf4j
@Service
public class RequestThrottlingService extends AbstractRateLimiterService {

    private static final String IP_CONFIG_NAME = "requestThrottlingConfig";

    @Autowired
    public RequestThrottlingService(RateLimiterRegistry rateLimiterRegistry) {
        super(rateLimiterRegistry, "throttle_");
    }

    /**
     * Check if a request from the given IP should be allowed based on throttling configuration.
     * Throws RateLimitExceededException if the rate limit is exceeded.
     *
     * @param ip the IP address
     * @throws RateLimitExceededException if the rate limit for this IP is exceeded
     */
    public void checkThrottlingForIp(String ip) {
        // The prefix is now handled by the base class
        checkRateLimitForIp(ip);
    }

    @Override
    protected String getIpConfigName() {
        return IP_CONFIG_NAME;
    }

    @Override
    protected String getEmailConfigName() {
        // Not used for request throttling
        return null;
    }

    @Override
    protected String getChatIdConfigName() {
        // Not used for request throttling
        return null;
    }

    @Override
    protected RateLimitExceededException createIpException(String ip) {
        return new RateLimitExceededException();
    }

    @Override
    protected RateLimitExceededException createEmailException(String email) {
        // Not used for request throttling
        return null;
    }

    @Override
    protected RateLimitExceededException createChatIdException(String chatId) {
        // Not used for request throttling
        return null;
    }
}