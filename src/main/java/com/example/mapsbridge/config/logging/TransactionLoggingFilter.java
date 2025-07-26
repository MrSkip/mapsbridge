package com.example.mapsbridge.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that extracts transaction ID from request headers and stores it in the LoggingContext.
 * If no transaction ID is provided in the headers, a new one is generated.
 * This filter runs early in the filter chain to ensure the transaction ID is available
 * for all subsequent processing.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String transactionId = extractTransactionId(request);
            LoggingContext.setTransactionId(transactionId);

            // Extract and store the client IP address
            String clientIp = extractClientIp(request);
            LoggingContext.setIpAddress(clientIp);
            
            // Add the transaction ID to the response headers for client tracking
            response.setHeader(TRANSACTION_ID_HEADER, transactionId);
            
            filterChain.doFilter(request, response);
        } finally {
            // Clear the context to prevent memory leaks
            LoggingContext.clear();
        }
    }

    private String extractTransactionId(HttpServletRequest request) {
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        
        if (!StringUtils.hasText(transactionId)) {
            // Generate a new transaction ID if none is provided
            transactionId = UUID.randomUUID().toString();
            log.debug("No transaction ID provided in request, generated new ID: {}", transactionId);
        } else {
            log.debug("Using transaction ID from request header: {}", transactionId);
        }
        
        return transactionId;
    }

    /**
     * Extract the client IP address from the request.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // In case of multiple proxies, the first IP is the client's
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}