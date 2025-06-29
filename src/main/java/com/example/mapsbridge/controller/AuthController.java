package com.example.mapsbridge.controller;

import com.example.mapsbridge.dto.ApiKeyResponseDto;
import com.example.mapsbridge.dto.KeyRequestDto;
import com.example.mapsbridge.dto.MessageResponseDto;
import com.example.mapsbridge.service.EmailConfirmationService;
import com.example.mapsbridge.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling API key authentication and generation.
 */
@Slf4j
@RestController
@RequestMapping("/auth/api")
public class AuthController {

    private final EmailConfirmationService emailConfirmationService;
    private final RateLimiterService rateLimiterService;

    @Autowired
    public AuthController(EmailConfirmationService emailConfirmationService, RateLimiterService rateLimiterService) {
        this.emailConfirmationService = emailConfirmationService;
        this.rateLimiterService = rateLimiterService;
    }

    /**
     * Endpoint for requesting an API key.
     * Sends a confirmation email with a token to the provided email address.
     *
     * @param request the request containing the email address
     * @param httpRequest the HTTP request
     * @return a response indicating success or failure
     */
    @PostMapping("/request-api-key")
    public ResponseEntity<MessageResponseDto> requestApiKey(
            @Valid @RequestBody KeyRequestDto request,
            HttpServletRequest httpRequest) {
        String email = request.getEmail();

        String clientIp = getClientIp(httpRequest);
        log.info("Received API key request for email: {} from IP: {}", email, clientIp);

        // Check rate limits - will throw exceptions if limits are exceeded
        rateLimiterService.checkRateLimit(clientIp);
        rateLimiterService.checkRateLimitForEmail(email);

        // This will throw exceptions if there are any issues
        MessageResponseDto response = emailConfirmationService.generateAndSendConfirmationToken(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Extract the client IP address from the request.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // In case of multiple proxies, the first IP is the client's
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Endpoint for confirming an email and generating an API key.
     * Validates the token and returns an API key if valid.
     *
     * @param token the confirmation token
     * @return a response containing the API key if successful, or an error message
     */
    @GetMapping("/confirm")
    public ResponseEntity<ApiKeyResponseDto> confirmEmail(
            @RequestParam String token,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        log.info("Received confirmation request with token: {} from IP: {}", token, clientIp);

        // Add rate limiting for confirmation attempts
        rateLimiterService.checkRateLimit(clientIp);

        // This will throw exceptions if there are any issues with the token
        ApiKeyResponseDto response = emailConfirmationService.validateTokenAndGenerateApiKey(token);
        return ResponseEntity.ok(response);
    }
}
