package com.example.mapsbridge.task;

import com.example.mapsbridge.service.RateLimiterService;
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

    private final RateLimiterService rateLimiterService;
    
    @Value("${app.rate-limiter.cleanup.max-idle-hours:24}")
    private int maxIdleHours;

    @Autowired
    public RateLimiterCleanupTask(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    /**
     * Cleans up old entries from IP and email rate limiter maps.
     * Runs daily at 1:00 AM.
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1:00 AM every day
    public void cleanupOldRateLimiters() {
        log.info("Starting scheduled cleanup of old rate limiter entries");
        
        int ipRemoved = rateLimiterService.cleanupOldIpRateLimiters(maxIdleHours);
        int emailRemoved = rateLimiterService.cleanupOldEmailRateLimiters(maxIdleHours);
        
        log.info("Removed {} old IP rate limiters and {} old email rate limiters", 
                ipRemoved, emailRemoved);
    }
}