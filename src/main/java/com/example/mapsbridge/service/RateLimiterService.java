
package com.example.mapsbridge.service;

import com.example.mapsbridge.exception.rate.EmailRateLimitExceededException;
import com.example.mapsbridge.exception.rate.IpRateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.ZoneOffset.UTC;

/**
 * Service for rate limiting requests by IP or email using Resilience4j.
 */
@Slf4j
@Service
public class RateLimiterService {

    private final RateLimiterRegistry rateLimiterRegistry;

    // Maps to track which rate limiter is used for which IP/email
    private final Map<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();
    private final Map<String, RateLimiter> emailRateLimiters = new ConcurrentHashMap<>();

    // Maps to track last access time for each IP/email
    private final Map<String, LocalDateTime> ipLastAccessTimes = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> emailLastAccessTimes = new ConcurrentHashMap<>();

    @Autowired
    public RateLimiterService(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    /**
     * Check if a request from the given IP should be allowed.
     * Throws IpRateLimitExceededException if the rate limit is exceeded.
     *
     * @param ip the IP address
     * @throws IpRateLimitExceededException if the rate limit for this IP is exceeded
     */
    public void checkRateLimit(String ip) {
        try {
            // Get or create a rate limiter for this specific IP using the ipConfig configuration
            RateLimiter limiter = ipRateLimiters.computeIfAbsent(ip,
                    k -> rateLimiterRegistry.rateLimiter(k, "ipConfig"));

            // Update last access time
            ipLastAccessTimes.put(ip, LocalDateTime.now());

            // Try to acquire permission
            if (!limiter.acquirePermission()) {
                throw new IpRateLimitExceededException(ip);
            }
        } catch (IpRateLimitExceededException e) {
            // Re-throw our custom exception
            throw e;
        } catch (Exception e) {
            log.error("Error checking rate limit for IP {}: {}", ip, e.getMessage());
            // In case of error, allow the request to avoid blocking legitimate users
        }
    }

    /**
     * Check if a request for the given email should be allowed.
     * Throws EmailRateLimitExceededException if the rate limit is exceeded.
     *
     * @param email the email address
     * @throws EmailRateLimitExceededException if the rate limit for this email is exceeded
     */
    public void checkRateLimitForEmail(String email) {
        try {
            // Get or create a rate limiter for this specific email using the emailConfig configuration
            RateLimiter limiter = emailRateLimiters.computeIfAbsent(email,
                    k -> rateLimiterRegistry.rateLimiter(k, "emailConfig"));

            // Update last access time
            emailLastAccessTimes.put(email, LocalDateTime.now(UTC));

            // Try to acquire permission
            if (!limiter.acquirePermission()) {
                throw new EmailRateLimitExceededException(email);
            }
        } catch (EmailRateLimitExceededException e) {
            // Re-throw our custom exception
            throw e;
        } catch (Exception e) {
            log.error("Error checking rate limit for email {}: {}", email, e.getMessage());
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
        return cleanupOldEntries(ipRateLimiters, ipLastAccessTimes, maxIdleHours);
    }

    /**
     * Removes email rate limiters that haven't been accessed for the specified number of hours.
     *
     * @param maxIdleHours maximum number of hours since last access before removing
     * @return number of entries removed
     */
    public int cleanupOldEmailRateLimiters(int maxIdleHours) {
        return cleanupOldEntries(emailRateLimiters, emailLastAccessTimes, maxIdleHours);
    }

    /**
     * Generic method to clean up old entries from a rate limiter map based on last access times.
     *
     * @param rateLimiters    map of rate limiters to clean up
     * @param lastAccessTimes map of last access times
     * @param maxIdleHours    maximum number of hours since last access before removing
     * @return number of entries removed
     */
    private int cleanupOldEntries(Map<String, RateLimiter> rateLimiters,
                                  Map<String, LocalDateTime> lastAccessTimes,
                                  int maxIdleHours) {
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
                log.debug("Removed rate limiter for key: {}, last accessed: {}", key, lastAccess);
            }
        }

        return removedCount;
    }
}