package com.example.mapsbridge.task;

import com.example.mapsbridge.service.ratelimit.MapConverterRateLimiterService;
import com.example.mapsbridge.service.ratelimit.RequestThrottlingService;
import com.example.mapsbridge.service.ratelimit.UserRateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to clean up old entries from rate limiter maps.
 */
@Slf4j
@Component
public class RateLimiterCleanupTask {

    private final UserRateLimiterService userRateLimiterService;
    private final MapConverterRateLimiterService mapConverterRateLimiterService;
    private final RequestThrottlingService requestThrottlingService;


    @Value("${app.rate-limiter.cleanup.max-idle-hours:24}")
    private int maxIdleHours;

    @Autowired
    public RateLimiterCleanupTask(UserRateLimiterService userRateLimiterService,
                                  MapConverterRateLimiterService mapConverterRateLimiterService,
                                  RequestThrottlingService requestThrottlingService) {
        this.userRateLimiterService = userRateLimiterService;
        this.mapConverterRateLimiterService = mapConverterRateLimiterService;
        this.requestThrottlingService = requestThrottlingService;
    }

    @Scheduled(cron = "0 0 * * * ?") // Run at the start of every hour
    public void cleanupOldRequestThrottlingEntries() {
        log.info("Starting hourly cleanup of old request throttling entries");

        // Clean up request throttling rate limiters
        int throttlingIpRemoved = requestThrottlingService.cleanupOldIpRateLimiters(1);

        if (throttlingIpRemoved > 0) {
            log.info("Removed {} old request throttling IP rate limiters", throttlingIpRemoved);
        } else {
            log.debug("No old request throttling entries to clean up");
        }
    }

    /**
     * Cleans up old entries from IP, email, and chat ID rate limiter maps.
     * Runs daily at 1:00 AM.
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1:00 AM every day
    public void cleanupOldRateLimiters() {
        log.info("Starting scheduled cleanup of old rate limiter entries");

        // Clean up user sign-up rate limiters
        int userIpRemoved = userRateLimiterService.cleanupOldIpRateLimiters(maxIdleHours);
        int userEmailRemoved = userRateLimiterService.cleanupOldEmailRateLimiters(maxIdleHours);
        int userChatIdRemoved = userRateLimiterService.cleanupOldChatIdRateLimiters(maxIdleHours);

        log.info("Removed {} old user IP rate limiters, {} old user email rate limiters, and {} old user chat ID rate limiters",
                userIpRemoved, userEmailRemoved, userChatIdRemoved);

        // Clean up geocoding rate limiters
        int geocodingIpRemoved = mapConverterRateLimiterService.cleanupOldIpRateLimiters(maxIdleHours);
        int geocodingEmailRemoved = mapConverterRateLimiterService.cleanupOldEmailRateLimiters(maxIdleHours);
        int geocodingChatIdRemoved = mapConverterRateLimiterService.cleanupOldChatIdRateLimiters(maxIdleHours);

        log.info("Removed {} old geocoding IP rate limiters, {} old geocoding email rate limiters, and {} old geocoding chat ID rate limiters",
                geocodingIpRemoved, geocodingEmailRemoved, geocodingChatIdRemoved);
    }
}