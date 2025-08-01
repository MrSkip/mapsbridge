package com.example.mapsbridge.aspect;

import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.config.metrics.tracker.IpAddressTracker;
import com.example.mapsbridge.config.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspect for tracking IP addresses of incoming API requests.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IpTrackingAspect {

    private final IpAddressTracker ipAddressTracker;

    private static String getIpAddress(HttpServletRequest request) {
        // First try to get IP from LoggingContext
        String ipAddress = LoggingContext.getIpAddress();

        // If not available, extract from request headers
        if (StringUtils.isBlank(ipAddress)) {
            ipAddress = IpUtils.getClientIp(request);
        }

        return ipAddress;
    }

    /**
     * Intercepts all controller methods in the API package and tracks the client IP address.
     */
    @Before("execution(* com.example.mapsbridge.controller.*.*(..))")
    public void trackIpAddress() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String ipAddress = getIpAddress(request);

                ipAddressTracker.trackIpAddress(ipAddress);
            }
        } catch (Exception e) {
            // Log error but don't disrupt the main flow
            log.error("Error tracking IP address: {}", e.getMessage(), e);
        }
    }

    /**
     * Gets the current HTTP request.
     *
     * @return The current HTTP request or null if not available
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}