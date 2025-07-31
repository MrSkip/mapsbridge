package com.example.mapsbridge.controller;

import com.example.mapsbridge.dto.request.KeyRequestDto;
import com.example.mapsbridge.dto.response.ApiKeyResponseDto;
import com.example.mapsbridge.dto.response.MessageResponseDto;
import com.example.mapsbridge.service.EmailConfirmationService;
import com.example.mapsbridge.service.ratelimit.UserRateLimiterService;
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
    private final UserRateLimiterService userRateLimiterService;

    @Autowired
    public AuthController(EmailConfirmationService emailConfirmationService, UserRateLimiterService userRateLimiterService) {
        this.emailConfirmationService = emailConfirmationService;
        this.userRateLimiterService = userRateLimiterService;
    }

    /**
     * Endpoint for requesting an API key.
     * Sends a confirmation email with a token to the provided email address.
     *
     * @param request the request containing the email address
     * @return a response indicating success or failure
     */
    @PostMapping("/request-api-key")
    public ResponseEntity<MessageResponseDto> requestApiKey(
            @Valid @RequestBody KeyRequestDto request) {

        // Check rate limits - will throw exceptions if limits are exceeded
        userRateLimiterService.checkIpRateLimit();
        userRateLimiterService.checkRateLimitForEmail(request.getEmail());

        // This will throw exceptions if there are any issues
        MessageResponseDto response = emailConfirmationService.generateAndSendConfirmationToken(request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for confirming an email and generating an API key.
     * Validates the token and returns an API key if valid.
     *
     * @param token the confirmation token
     * @return a response containing the API key if successful, or an error message
     */
    @GetMapping("/confirm")
    public ResponseEntity<ApiKeyResponseDto> confirmEmail(@RequestParam String token) {
        // Add rate limiting for confirmation attempts
        userRateLimiterService.checkIpRateLimit();

        // This will throw exceptions if there are any issues with the token
        ApiKeyResponseDto response = emailConfirmationService.validateTokenAndGenerateApiKey(token);
        return ResponseEntity.ok(response);
    }
}
