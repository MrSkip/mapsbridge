package com.example.mapsbridge.config.auth;

import com.example.mapsbridge.config.auth.security.ApiKeyAuthToken;
import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.model.ApiKeyModel;
import com.example.mapsbridge.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Component
public class ApiKeyAuthManager implements AuthenticationManager {

    private static final int TOKEN_LOG_PREFIX_LENGTH = 5;
    private static final String TOKEN_LOG_SUFFIX = "...";

    private final String masterToken;
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthManager(@Value("${api.security.token}") String masterToken,
                             ApiKeyRepository apiKeyRepository) {
        this.masterToken = masterToken;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof ApiKeyAuthToken authToken)) {
            log.warn("Unsupported authentication type: {}", authentication.getClass().getName());
            throw new UnsupportedAuthenticationException("Unsupported authentication type: " + authentication.getClass().getName());
        }

        String providedToken = extractToken(authToken);
        log.debug("Authenticating token: {}", maskToken(providedToken));

        return authenticateToken(providedToken, authToken);
    }

    private Authentication authenticateToken(String providedToken, ApiKeyAuthToken authToken) {
        if (isMasterToken(providedToken)) {
            log.info("Master token authentication successful");
            LoggingContext.setEmail("master@token");
            return ApiKeyAuthToken.createMasterToken(providedToken);
        }

        if (isValidApiKey(providedToken)) {
            log.debug("API key authentication successful, creating user token with ROLE_API_USER");
            ApiKeyAuthToken token = ApiKeyAuthToken.createUserToken(providedToken);
            log.debug("Created token with authorities: {}", token.getAuthorities());
            return token;
        }

        return handleAuthenticationFailure(providedToken, authToken);
    }

    private String extractToken(ApiKeyAuthToken authToken) {
        Object principal = authToken.getPrincipal();
        if (!(principal instanceof String token)) {
            throw new InvalidTokenException("Token must be a string");
        }
        return token;
    }

    private Authentication handleAuthenticationFailure(String providedToken, ApiKeyAuthToken authToken) {
        log.warn("Authentication failed for token: {}", maskToken(providedToken));
        authToken.setAuthenticated(false);
        return authToken;
    }

    private boolean isMasterToken(String token) {
        return masterToken.equals(token);
    }

    private boolean isValidApiKey(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token is null or empty");
            return false;
        }

        log.debug("Checking if token is a valid API key: {}", maskToken(token));

        return findApiKey(token)
                .map(this::validateAndUpdateApiKey)
                .orElseGet(() -> {
                    log.warn("API key not found in repository");
                    return false;
                });
    }

    private Optional<ApiKeyModel> findApiKey(String token) {
        try {
            return apiKeyRepository.findByApiKey(token);
        } catch (Exception e) {
            log.error("Error querying API key repository", e);
            return Optional.empty();
        }
    }

    private boolean validateAndUpdateApiKey(ApiKeyModel apiKey) {
        log.debug("API key found in repository, active: {}", apiKey.isActive());

        if (!apiKey.isActive()) {
            log.warn("API key is not active");
            return false;
        }

        // Store email in LoggingContext for inclusion in logs
        String email = apiKey.getEmail();
        if (email != null && !email.isEmpty()) {
            LoggingContext.setEmail(email);
            log.debug("Set email in logging context: {}", email);
        }

        updateLastUsedTimestamp(apiKey);
        return true;
    }

    private void updateLastUsedTimestamp(ApiKeyModel apiKey) {
        try {
            apiKey.setLastUsedAt(LocalDateTime.now(UTC));
            apiKeyRepository.save(apiKey);
        } catch (Exception e) {
            log.warn("Failed to update last used timestamp for API key", e);
        }
    }

    private String maskToken(String token) {
        if (token == null) {
            return "null";
        }
        if (token.length() <= TOKEN_LOG_PREFIX_LENGTH) {
            return "*".repeat(token.length());
        }
        return token.substring(0, TOKEN_LOG_PREFIX_LENGTH) + TOKEN_LOG_SUFFIX;
    }

    // Custom exceptions for better error handling
    public static class UnsupportedAuthenticationException extends AuthenticationException {
        public UnsupportedAuthenticationException(String message) {
            super(message);
        }
    }

    public static class InvalidTokenException extends AuthenticationException {
        public InvalidTokenException(String message) {
            super(message);
        }
    }
}
