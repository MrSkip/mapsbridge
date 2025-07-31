package com.example.mapsbridge.aspect;

import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.config.metrics.tracker.ClientTracker;
import com.example.mapsbridge.service.ratelimit.MapConverterRateLimiterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Aspect to apply rate limiting to map conversion operations.
 * Checks rate limits for IP address, chat ID, and email before allowing the method execution.
 */
@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class MapConverterRateLimitAspect {

    private final MapConverterRateLimiterService mapConverterRateLimiterService;
    private final ClientTracker clientTracker;

    /**
     * Pointcut that matches the convert method in implementations of the MapConverterService interface.
     */
    @Pointcut("execution(* com.example.mapsbridge.service.mapconverter.MapConverterService+.convert(..))")
    public void mapConverterMethods() {
    }

    /**
     * Advice that checks rate limits before allowing the method execution.
     * Checks rate limits for IP address, chat ID, and email if they are available in the LoggingContext.
     *
     * @param joinPoint the join point
     * @return the result of the method execution
     * @throws Throwable if an error occurs during method execution or if a rate limit is exceeded
     */
    @Around("mapConverterMethods()")
    public Object checkRateLimits(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        log.debug("Checking rate limits for map conversion operation: {}", methodName);

        // Apply only one limit at a time based on priority:
        // 1. If email is present, limit by email (user is using custom API call)
        // 2. If email is not present but IP is present, limit by IP (user is using website)
        // 3. If chatId is present, limit by chatId (user is using chat)

        String email = LoggingContext.getEmail();
        if (StringUtils.hasText(email)) {
            // User is using custom API call
            log.debug("Checking geocoding daily quota for email: {}", email);
            mapConverterRateLimiterService.checkDailyQuotaForEmail(email);
            clientTracker.trackApiRequest();
        } else {
            String ipAddress = LoggingContext.getIpAddress();
            if (StringUtils.hasText(ipAddress)) {
                // User is using website
                log.debug("Checking geocoding daily quota for IP: {}", ipAddress);
                mapConverterRateLimiterService.checkDailyQuotaForIp(ipAddress);
                clientTracker.trackWebRequest();
            } else {
                String chatId = LoggingContext.getChatId();
                if (StringUtils.hasText(chatId)) {
                    // User is using chat
                    log.debug("Checking geocoding daily quota for chat ID: {}", chatId);
                    mapConverterRateLimiterService.checkDailyQuotaForChatId(chatId);
                    clientTracker.trackTelegramRequest();
                }
            }
        }

        // If rate limit check passes, proceed with the method execution
        return joinPoint.proceed();
    }
}