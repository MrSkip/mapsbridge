package com.example.mapsbridge.aspect;

import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.config.metrics.MetricTags;
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

import java.util.Map;
import java.util.function.Consumer;

/**
 * Aspect to apply rate limiting to map conversion operations.
 * Checks rate limits for IP address, chat ID, and email before allowing the method execution.
 * Also tracks requests based on the endpoint type when applicable.
 */
@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class MapConverterRateLimitAspect {

    private final MapConverterRateLimiterService mapConverterRateLimiterService;
    private final ClientTracker clientTracker;

    private static final Map<String, Consumer<ClientTracker>> ENDPOINT_TRACKERS = Map.of(
            MetricTags.SDK.toLowerCase(), ClientTracker::trackSdkRequest,
            MetricTags.SHORTCUT.toLowerCase(), ClientTracker::trackShortcutRequest,
            MetricTags.WEB.toLowerCase(), ClientTracker::trackWebRequest
    );

    /**
     * Pointcut that matches the convert method in implementations of the MapConverterService interface.
     */
    @Pointcut("execution(* com.example.mapsbridge.service.converter.MapConverterService+.convert(..))")
    public void mapConverterMethods() {
    }

    /**
     * Advice that checks rate limits before allowing the method execution.
     * Priority order: email -> IP address -> chat ID
     */
    @Around("mapConverterMethods()")
    public Object checkRateLimits(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Checking rate limits for map conversion operation: {}", joinPoint.getSignature().getName());

        if (handleEmailRateLimit()) {
            return joinPoint.proceed();
        }

        if (handleIpRateLimit()) {
            return joinPoint.proceed();
        }

        handleChatRateLimit();
        return joinPoint.proceed();
    }

    private boolean handleEmailRateLimit() {
        String email = LoggingContext.getEmail();
        if (!StringUtils.hasText(email)) {
            return false;
        }

        log.debug("Checking geocoding daily quota for email: {}", email);
        mapConverterRateLimiterService.checkDailyQuotaForEmail(email);
        clientTracker.trackApiRequest();
        return true;
    }

    private boolean handleIpRateLimit() {
        String ipAddress = LoggingContext.getIpAddress();
        if (!StringUtils.hasText(ipAddress)) {
            return false;
        }

        log.debug("Checking geocoding daily quota for IP: {}", ipAddress);
        mapConverterRateLimiterService.checkDailyQuotaForIp(ipAddress);
        trackEndpointRequest();
        return true;
    }

    private void handleChatRateLimit() {
        String chatId = LoggingContext.getChatId();
        if (StringUtils.hasText(chatId)) {
            log.debug("Checking geocoding daily quota for chat ID: {}", chatId);
            mapConverterRateLimiterService.checkDailyQuotaForChatId(chatId);
            clientTracker.trackTelegramRequest();
        }
    }

    private void trackEndpointRequest() {
        String endpointType = LoggingContext.getEndpointType();

        if (!StringUtils.hasText(endpointType)) {
            log.warn("Endpoint type is not set in LoggingContext, cannot track request source");
            return;
        }

        Consumer<ClientTracker> tracker = ENDPOINT_TRACKERS.get(endpointType);
        if (tracker != null) {
            tracker.accept(clientTracker);
        } else {
            log.warn("Unknown endpoint type: {}", endpointType);
        }
    }
}