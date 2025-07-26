package com.example.mapsbridge.service.ratelimit;

import com.example.mapsbridge.exception.rate.RateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.time.ZoneOffset.UTC;

/**
 * Abstract base class for rate limiting services.
 * Provides common functionality for different types of rate limiting.
 */
@Slf4j
public abstract class AbstractRateLimiterService {

    protected final RateLimiterRegistry rateLimiterRegistry;

    // Maps to track which rate limiter is used for which identifier
    protected final Map<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();
    protected final Map<String, RateLimiter> emailRateLimiters = new ConcurrentHashMap<>();
    protected final Map<String, RateLimiter> chatIdRateLimiters = new ConcurrentHashMap<>();

    // Maps to track last access time for each identifier
    protected final Map<String, LocalDateTime> ipLastAccessTimes = new ConcurrentHashMap<>();
    protected final Map<String, LocalDateTime> emailLastAccessTimes = new ConcurrentHashMap<>();
    protected final Map<String, LocalDateTime> chatIdLastAccessTimes = new ConcurrentHashMap<>();

    protected AbstractRateLimiterService(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    /**
     * Get the configuration name for IP rate limiting.
     *
     * @return the configuration name
     */
    protected abstract String getIpConfigName();

    /**
     * Get the configuration name for email rate limiting.
     *
     * @return the configuration name
     */
    protected abstract String getEmailConfigName();

    /**
     * Get the configuration name for chat ID rate limiting.
     *
     * @return the configuration name
     */
    protected abstract String getChatIdConfigName();

    /**
     * Create a rate limit exceeded exception for IP.
     *
     * @param ip the IP address
     * @return the exception
     */
    protected abstract RateLimitExceededException createIpException(String ip);

    /**
     * Create a rate limit exceeded exception for email.
     *
     * @param email the email address
     * @return the exception
     */
    protected abstract RateLimitExceededException createEmailException(String email);

    /**
     * Create a rate limit exceeded exception for chat ID.
     *
     * @param chatId the chat ID
     * @return the exception
     */
    protected abstract RateLimitExceededException createChatIdException(String chatId);

    /**
     * Check if a request from the given IP should be allowed.
     * Throws a rate limit exceeded exception if the rate limit is exceeded.
     *
     * @param ip the IP address
     * @throws RateLimitExceededException if the rate limit for this IP is exceeded
     */
    public void checkRateLimitForIp(String ip) {
        checkRateLimit(ip, "IP", ipRateLimiters, ipLastAccessTimes,
                getIpConfigName(), this::createIpException);
    }

    /**
     * Check if a request for the given email should be allowed.
     * Throws a rate limit exceeded exception if the rate limit is exceeded.
     *
     * @param email the email address
     * @throws RateLimitExceededException if the rate limit for this email is exceeded
     */
    public void checkRateLimitForEmail(String email) {
        checkRateLimit(email, "email", emailRateLimiters, emailLastAccessTimes,
                getEmailConfigName(), this::createEmailException);
    }

    /**
     * Check if a request for the given chat ID should be allowed.
     * Throws a rate limit exceeded exception if the rate limit is exceeded.
     *
     * @param chatId the chat ID
     * @throws RateLimitExceededException if the rate limit for this chat ID is exceeded
     */
    public void checkRateLimitForChatId(String chatId) {
        checkRateLimit(chatId, "chat ID", chatIdRateLimiters, chatIdLastAccessTimes,
                getChatIdConfigName(), this::createChatIdException);
    }

    /**
     * Generic method to check rate limits for any identifier type.
     *
     * @param identifier       the identifier to check
     * @param identifierType   the type of identifier (for logging)
     * @param rateLimiters     the rate limiter map
     * @param lastAccessTimes  the last access times map
     * @param configName       the configuration name
     * @param exceptionFactory function to create the appropriate exception
     * @throws RateLimitExceededException if the rate limit is exceeded
     */
    protected void checkRateLimit(String identifier,
                                  String identifierType,
                                  Map<String, RateLimiter> rateLimiters,
                                  Map<String, LocalDateTime> lastAccessTimes,
                                  String configName,
                                  Function<String, RateLimitExceededException> exceptionFactory) {
        if (StringUtils.isBlank(identifier)) {
            log.warn("Empty or null {} provided for rate limiting", identifierType);
            return;
        }

        try {
            // Get or create a rate limiter for this specific identifier
            RateLimiter limiter = rateLimiters.computeIfAbsent(identifier,
                    k -> rateLimiterRegistry.rateLimiter(k, configName));

            // Update last access time
            lastAccessTimes.put(identifier, LocalDateTime.now(UTC));

            // Try to acquire permission
            if (!limiter.acquirePermission()) {
                log.warn("Rate limit exceeded for {} {}", identifierType, identifier);
                throw exceptionFactory.apply(identifier);
            }

            log.debug("Rate limit check passed for {} {}", identifierType, identifier);
        } catch (RateLimitExceededException e) {
            // Re-throw our custom exception
            throw e;
        } catch (Exception e) {
            log.error("Error checking rate limit for {} {}: {}", identifierType, identifier, e.getMessage(), e);
            // In case of error, allow the request to avoid blocking legitimate users
        }
    }

    /**
     * Removes IP rate limiters that haven't been accessed for the specified number of hours.
     *
     * @param maxIdleHours maximum number of hours since last access before removing
     * @return number of entries removed
     */
    public int cleanupOldIpRateLimiters(int maxIdleHours) {
        return cleanupOldEntries("IP", ipRateLimiters, ipLastAccessTimes, maxIdleHours);
    }

    /**
     * Removes email rate limiters that haven't been accessed for the specified number of hours.
     *
     * @param maxIdleHours maximum number of hours since last access before removing
     * @return number of entries removed
     */
    public int cleanupOldEmailRateLimiters(int maxIdleHours) {
        return cleanupOldEntries("email", emailRateLimiters, emailLastAccessTimes, maxIdleHours);
    }

    /**
     * Removes chat ID rate limiters that haven't been accessed for the specified number of hours.
     *
     * @param maxIdleHours maximum number of hours since last access before removing
     * @return number of entries removed
     */
    public int cleanupOldChatIdRateLimiters(int maxIdleHours) {
        return cleanupOldEntries("chat ID", chatIdRateLimiters, chatIdLastAccessTimes, maxIdleHours);
    }

    /**
     * Generic method to clean up old entries from a rate limiter map based on last access times.
     *
     * @param identifierType  the type of identifier (for logging)
     * @param rateLimiters    map of rate limiters to clean up
     * @param lastAccessTimes map of last access times
     * @param maxIdleHours    maximum number of hours since last access before removing
     * @return number of entries removed
     */
    protected int cleanupOldEntries(String identifierType,
                                    Map<String, RateLimiter> rateLimiters,
                                    Map<String, LocalDateTime> lastAccessTimes,
                                    int maxIdleHours) {
        if (maxIdleHours <= 0) {
            log.warn("Invalid maxIdleHours value: {}. Skipping cleanup.", maxIdleHours);
            return 0;
        }

        LocalDateTime cutoffTime = LocalDateTime.now(UTC).minusHours(maxIdleHours);
        int removedCount = 0;

        // Use iterator to safely remove entries during iteration
        Iterator<Map.Entry<String, LocalDateTime>> iterator = lastAccessTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, LocalDateTime> entry = iterator.next();
            String key = entry.getKey();
            LocalDateTime lastAccess = entry.getValue();

            if (lastAccess.isBefore(cutoffTime)) {
                // Remove from both maps
                rateLimiters.remove(key);
                iterator.remove(); // Safe way to remove from the map we're iterating
                removedCount++;
                log.debug("Removed {} rate limiter for key: {}, last accessed: {}",
                        identifierType, key, lastAccess);
            }
        }

        if (removedCount > 0) {
            log.info("Cleaned up {} old {} rate limiter entries", removedCount, identifierType);
        }

        return removedCount;
    }

    /**
     * Get the current number of active IP rate limiters.
     *
     * @return the number of active IP rate limiters
     */
    public int getActiveIpRateLimitersCount() {
        return ipRateLimiters.size();
    }

    /**
     * Get the current number of active email rate limiters.
     *
     * @return the number of active email rate limiters
     */
    public int getActiveEmailRateLimitersCount() {
        return emailRateLimiters.size();
    }

    /**
     * Get the current number of active chat ID rate limiters.
     *
     * @return the number of active chat ID rate limiters
     */
    public int getActiveChatIdRateLimitersCount() {
        return chatIdRateLimiters.size();
    }
}