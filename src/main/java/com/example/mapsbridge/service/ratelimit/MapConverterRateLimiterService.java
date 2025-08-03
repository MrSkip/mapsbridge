package com.example.mapsbridge.service.ratelimit;

import com.example.mapsbridge.exception.rate.ChatIdRateLimitExceededException;
import com.example.mapsbridge.exception.rate.EmailRateLimitExceededException;
import com.example.mapsbridge.exception.rate.IpRateLimitExceededException;
import com.example.mapsbridge.exception.rate.RateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for rate limiting geocoding API calls by IP, email, or chat ID using Resilience4j.
 * Uses daily quotas for geocoding API calls.
 */
@Slf4j
@Service
public class MapConverterRateLimiterService extends AbstractRateLimiterService {

    private static final String IP_CONFIG_NAME = "geocodingIpConfig";
    private static final String EMAIL_CONFIG_NAME = "geocodingEmailConfig";
    private static final String CHAT_ID_CONFIG_NAME = "geocodingChatIdConfig";

    @Autowired
    public MapConverterRateLimiterService(RateLimiterRegistry rateLimiterRegistry) {
        super(rateLimiterRegistry, "daily_");
    }

    /**
     * Check if a geocoding request from the given IP should be allowed based on daily quota.
     * Throws IpRateLimitExceededException if the daily quota is exceeded.
     *
     * @param ip the IP address
     * @throws IpRateLimitExceededException if the daily quota for this IP is exceeded
     */
    public void checkDailyQuotaForIp(String ip) {
        checkRateLimitForIp(ip);
    }

    /**
     * Check if a geocoding request for the given email should be allowed based on daily quota.
     * Throws EmailRateLimitExceededException if the daily quota is exceeded.
     *
     * @param email the email address
     * @throws EmailRateLimitExceededException if the daily quota for this email is exceeded
     */
    public void checkDailyQuotaForEmail(String email) {
        checkRateLimitForEmail(email);
    }

    /**
     * Check if a geocoding request for the given chat ID should be allowed based on daily quota.
     * Throws ChatIdRateLimitExceededException if the daily quota is exceeded.
     *
     * @param chatId the chat ID
     * @throws ChatIdRateLimitExceededException if the daily quota for this chat ID is exceeded
     */
    public void checkDailyQuotaForChatId(String chatId) {
        checkRateLimitForChatId(chatId);
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