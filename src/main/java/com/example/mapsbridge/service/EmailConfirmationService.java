package com.example.mapsbridge.service;

import com.example.mapsbridge.dto.ApiKeyResponseDto;
import com.example.mapsbridge.dto.MessageResponseDto;

/**
 * Service interface for email confirmation and API key generation.
 */
public interface EmailConfirmationService {

    /**
     * Generate a confirmation token for the given email and send it via email.
     *
     * @param email the email address to send the confirmation to
     * @return a response indicating success or failure
     */
    MessageResponseDto generateAndSendConfirmationToken(String email);

    /**
     * Validate a confirmation token and generate an API key if valid.
     *
     * @param token the confirmation token
     * @return a response containing the API key if successful, or an error message
     */
    ApiKeyResponseDto validateTokenAndGenerateApiKey(String token);

    /**
     * Generate a secure API key for the given email.
     *
     * @param email the email address to generate the API key for
     * @return the generated API key
     */
    String generateApiKey(String email);
}
