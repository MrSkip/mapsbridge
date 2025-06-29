package com.example.mapsbridge.task;

import com.example.mapsbridge.repository.EmailConfirmTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task to clean up expired tokens from the database.
 */
@Slf4j
@Component
public class TokenCleanupTask {

    private final EmailConfirmTokenRepository tokenRepository;

    @Autowired
    public TokenCleanupTask(EmailConfirmTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Deletes expired tokens from the database.
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired tokens");
        LocalDateTime now = LocalDateTime.now();
        
        long deletedCount = tokenRepository.deleteByExpiresAtBefore(now);
        
        log.info("Deleted {} expired tokens", deletedCount);
    }
}