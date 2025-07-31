package com.example.mapsbridge.service.impl;

import com.example.mapsbridge.dto.response.ApiKeyResponseDto;
import com.example.mapsbridge.dto.response.MessageResponseDto;
import com.example.mapsbridge.exception.InvalidTokenException;
import com.example.mapsbridge.exception.TokenExpiredException;
import com.example.mapsbridge.model.ApiKeyModel;
import com.example.mapsbridge.model.EmailConfirmTokenModel;
import com.example.mapsbridge.repository.ApiKeyRepository;
import com.example.mapsbridge.repository.EmailConfirmTokenRepository;
import com.example.mapsbridge.service.EmailConfirmationService;
import com.example.mapsbridge.service.MailtrapService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

/**
 * Implementation of the EmailConfirmationService interface.
 */
@Slf4j
@Service
public class EmailConfirmationServiceImpl implements EmailConfirmationService {

    private final EmailConfirmTokenRepository tokenRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final MailtrapService mailtrapService;

    @Value("${app.token.expiration-minutes:15}")
    private int tokenExpirationMinutes;

    @Value("${app.confirmation.base-url:http://localhost:8080}")
    private String baseUrl;
    @Value("${app.confirmation.base-url-suffix}")
    private String baseUrlSuffix;

    @Autowired
    public EmailConfirmationServiceImpl(
            EmailConfirmTokenRepository tokenRepository,
            ApiKeyRepository apiKeyRepository,
            MailtrapService mailtrapService) {
        this.tokenRepository = tokenRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.mailtrapService = mailtrapService;
    }

    @Override
    @Transactional
    public MessageResponseDto generateAndSendConfirmationToken(String email) {
        // Generate a unique token
        String token = UUID.randomUUID().toString();

        // Calculate expiration time
        LocalDateTime expiresAt = LocalDateTime.now(UTC).plusMinutes(tokenExpirationMinutes);

        // Save the token to the database
        EmailConfirmTokenModel confirmToken = EmailConfirmTokenModel.builder()
                .email(email)
                .token(token)
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(confirmToken);

        boolean emailSent = sendConfirmationEmail(email, token);

        if (!emailSent) {
            log.error("Failed to send confirmation email to: {}", email);
            throw new RuntimeException("Failed to send confirmation email. Please try again later.");
        }

        return MessageResponseDto.builder()
                .message("Confirmation email sent. Please check your inbox.")
                .success(true)
                .build();
    }

    private boolean sendConfirmationEmail(String email, String token) {
        // Build confirmation URL
        String confirmationUrl = baseUrl + baseUrlSuffix + token;

        // Generate human-readable expiration time
        String expirationTime = formatExpirationTime(tokenExpirationMinutes);

        // Build template model
        Map<String, Object> model = new HashMap<>();
        model.put("name", "there");
        model.put("confirmationUrl", confirmationUrl);
        model.put("expirationTime", expirationTime);

        return mailtrapService.sendTemplateEmail(
                email,
                "Confirm your email for Maps Bridge API key",
                "email-confirmation-template",
                model
        );
    }

    /**
     * Formats the expiration time in minutes to a human-readable string.
     *
     * @param minutes the number of minutes until expiration
     * @return formatted string like "in 15 minutes", "in 1 hour", etc.
     */
    private String formatExpirationTime(int minutes) {
        if (minutes < 60) {
            return String.format("in %d minute%s", minutes, minutes == 1 ? "" : "s");
        } else {
            int hours = minutes / 60;
            return String.format("in %d hour%s", hours, hours == 1 ? "" : "s");
        }
    }

    @Override
    @Transactional
    public ApiKeyResponseDto validateTokenAndGenerateApiKey(String token) {
        String email = handleEmailConfirmation(token);
        String apiKey = generateApiKey(email);

        removeOldKeys(email);

        ApiKeyModel newApiKey = ApiKeyModel.builder()
                .email(email)
                .apiKey(apiKey)
                .active(true)
                .build();

        apiKeyRepository.save(newApiKey);

        return ApiKeyResponseDto.builder()
                .apiKey(apiKey)
                .build();
    }

    private void removeOldKeys(String email) {
        int numberOfRemovedEntries = apiKeyRepository.removeByEmail(email);
        log.info("Removed {} existing API keys for email: {}", numberOfRemovedEntries, email);
    }

    private String handleEmailConfirmation(String token) {
        // Find the token in the database
        EmailConfirmTokenModel confirmToken = tokenRepository.findByToken(token)
                .orElseThrow(InvalidTokenException::new);

        // Check if the token is valid
        if (!confirmToken.isValid()) {
            throw new TokenExpiredException("Token has expired");
        }

        // Mark the token as used
        confirmToken.setUsed(true);
        tokenRepository.save(confirmToken);

        // Generate and save the API key
        return confirmToken.getEmail();
    }

    @Override
    public String generateApiKey(String email) {
        // Use SecureRandom for better entropy
        SecureRandom secureRandom = new SecureRandom();
        String randomPart = RandomStringUtils.random(24, 0, 0, true, true, null, secureRandom);
        return "maps_live_" + randomPart;
    }
}
