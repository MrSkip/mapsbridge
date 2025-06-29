package com.example.mapsbridge.service;

import com.example.mapsbridge.dto.ApiKeyResponseDto;
import com.example.mapsbridge.dto.MessageResponseDto;
import com.example.mapsbridge.exception.InvalidTokenException;
import com.example.mapsbridge.exception.TokenExpiredException;
import com.example.mapsbridge.model.ApiKeyModel;
import com.example.mapsbridge.model.EmailConfirmTokenModel;
import com.example.mapsbridge.repository.ApiKeyRepository;
import com.example.mapsbridge.repository.EmailConfirmTokenRepository;
import com.example.mapsbridge.service.impl.EmailConfirmationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("EmailConfirmationService Tests")
class EmailConfirmationServiceTest {

    @Mock
    private EmailConfirmTokenRepository tokenRepository;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private MailtrapService mailtrapService;

    private EmailConfirmationService emailConfirmationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailConfirmationService = new EmailConfirmationServiceImpl(tokenRepository, apiKeyRepository, mailtrapService);

        // Set properties using ReflectionTestUtils
        ReflectionTestUtils.setField(emailConfirmationService, "tokenExpirationMinutes", 15);
        ReflectionTestUtils.setField(emailConfirmationService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(emailConfirmationService, "baseUrlSuffix", "/auth/api/confirm?token=");
    }

    @Nested
    @DisplayName("Generate and Send Confirmation Token Tests")
    class GenerateAndSendConfirmationTokenTests {

        @Test
        @DisplayName("Should successfully generate and send confirmation token")
        void generateAndSendConfirmationToken_Success() {
            // Arrange
            String email = "test@example.com";
            when(apiKeyRepository.existsByEmailAndActive(email, true)).thenReturn(false);
            when(mailtrapService.sendTemplateEmail(eq(email), anyString(), anyString(), any(HashMap.class))).thenReturn(true);

            // Act
            MessageResponseDto response = emailConfirmationService.generateAndSendConfirmationToken(email);

            // Assert
            assertTrue(response.isSuccess());
            assertEquals("Confirmation email sent. Please check your inbox.", response.getMessage());

            // Verify token was saved
            ArgumentCaptor<EmailConfirmTokenModel> tokenCaptor = ArgumentCaptor.forClass(EmailConfirmTokenModel.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            EmailConfirmTokenModel savedToken = tokenCaptor.getValue();
            assertEquals(email, savedToken.getEmail());
            assertFalse(savedToken.isUsed());
            assertNotNull(savedToken.getToken());
            assertNotNull(savedToken.getExpiresAt());
            assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now(UTC)));

            // Verify email was sent with correct template
            verify(mailtrapService).sendTemplateEmail(
                    eq(email),
                    eq("Confirm your email for Maps Bridge API key"),
                    eq("email-confirmation-template"),
                    any(HashMap.class)
            );
        }

        @Test
        @DisplayName("Should generate new confirmation token when email already has active key")
        void generateAndSendConfirmationToken_EmailAlreadyHasActiveKey() {
            // Arrange
            String email = "test@example.com";
            when(apiKeyRepository.existsByEmailAndActive(email, true)).thenReturn(true);
            when(mailtrapService.sendTemplateEmail(eq(email), anyString(), anyString(), any(HashMap.class))).thenReturn(true);

            // Act
            MessageResponseDto response = emailConfirmationService.generateAndSendConfirmationToken(email);

            // Assert
            assertTrue(response.isSuccess());
            assertEquals("Confirmation email sent. Please check your inbox.", response.getMessage());

            // Verify token was saved even though email has active key
            ArgumentCaptor<EmailConfirmTokenModel> tokenCaptor = ArgumentCaptor.forClass(EmailConfirmTokenModel.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            EmailConfirmTokenModel savedToken = tokenCaptor.getValue();
            assertEquals(email, savedToken.getEmail());
            assertFalse(savedToken.isUsed());
            assertNotNull(savedToken.getToken());
            assertNotNull(savedToken.getExpiresAt());
            assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now(UTC)));

            // Verify email was sent even though email has active key
            verify(mailtrapService).sendTemplateEmail(
                    eq(email),
                    eq("Confirm your email for Maps Bridge API key"),
                    eq("email-confirmation-template"),
                    any(HashMap.class)
            );
        }

        @Test
        @DisplayName("Should throw exception when email sending fails")
        void generateAndSendConfirmationToken_EmailSendingFails() {
            // Arrange
            String email = "test@example.com";
            when(apiKeyRepository.existsByEmailAndActive(email, true)).thenReturn(false);
            when(mailtrapService.sendTemplateEmail(eq(email), anyString(), anyString(), any(HashMap.class))).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                emailConfirmationService.generateAndSendConfirmationToken(email);
            });

            assertEquals("Failed to send confirmation email. Please try again later.", exception.getMessage());

            // Verify token was saved (will be cleaned up by scheduled task)
            verify(tokenRepository).save(any());
        }

        @Test
        @DisplayName("Should handle email template variables correctly")
        void generateAndSendConfirmationToken_EmailTemplateVariables() {
            // Arrange
            String email = "test@example.com";
            when(apiKeyRepository.existsByEmailAndActive(email, true)).thenReturn(false);
            when(mailtrapService.sendTemplateEmail(eq(email), anyString(), anyString(), any(HashMap.class))).thenReturn(true);

            // Act
            emailConfirmationService.generateAndSendConfirmationToken(email);

            // Assert
            ArgumentCaptor<HashMap> templateModelCaptor = ArgumentCaptor.forClass(HashMap.class);
            verify(mailtrapService).sendTemplateEmail(
                    eq(email),
                    anyString(),
                    anyString(),
                    templateModelCaptor.capture()
            );

            HashMap<String, Object> templateModel = templateModelCaptor.getValue();
            assertEquals("there", templateModel.get("name"));
            assertTrue(templateModel.get("confirmationUrl").toString().contains("token="));
            assertEquals("in 15 minutes", templateModel.get("expirationTime"));
        }
    }

    @Nested
    @DisplayName("Validate Token and Generate API Key Tests")
    class ValidateTokenAndGenerateApiKeyTests {

        @Test
        @DisplayName("Should successfully validate token and generate API key")
        void validateTokenAndGenerateApiKey_Success() {
            // Arrange
            String token = UUID.randomUUID().toString();
            String email = "test@example.com";

            EmailConfirmTokenModel confirmToken = EmailConfirmTokenModel.builder()
                    .id(UUID.randomUUID())
                    .email(email)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .used(false)
                    .build();

            when(tokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));
            when(apiKeyRepository.removeByEmail(email)).thenReturn(0);

            // Act
            ApiKeyResponseDto response = emailConfirmationService.validateTokenAndGenerateApiKey(token);

            // Assert
            assertNotNull(response.getApiKey());
            assertTrue(response.getApiKey().startsWith("maps_live_"));

            // Verify token was marked as used
            assertTrue(confirmToken.isUsed());
            verify(tokenRepository).save(confirmToken);

            // Verify old keys were removed
            verify(apiKeyRepository).removeByEmail(email);

            // Verify new API key was saved
            ArgumentCaptor<ApiKeyModel> apiKeyCaptor = ArgumentCaptor.forClass(ApiKeyModel.class);
            verify(apiKeyRepository).save(apiKeyCaptor.capture());

            ApiKeyModel savedApiKey = apiKeyCaptor.getValue();
            assertEquals(email, savedApiKey.getEmail());
            assertTrue(savedApiKey.isActive());
            assertEquals(response.getApiKey(), savedApiKey.getApiKey());
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void validateTokenAndGenerateApiKey_InvalidToken() {
            // Arrange
            String token = "invalid-token";
            when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

            // Act & Assert
            InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> {
                emailConfirmationService.validateTokenAndGenerateApiKey(token);
            });

            assertEquals("Invalid token", exception.getMessage());

            // Verify no API key was saved
            verify(apiKeyRepository, never()).save(any());
            verify(apiKeyRepository, never()).removeByEmail(any());
        }

        @Test
        @DisplayName("Should throw exception for expired token")
        void validateTokenAndGenerateApiKey_ExpiredToken() {
            // Arrange
            String token = UUID.randomUUID().toString();
            String email = "test@example.com";

            EmailConfirmTokenModel confirmToken = EmailConfirmTokenModel.builder()
                    .id(UUID.randomUUID())
                    .email(email)
                    .token(token)
                    .expiresAt(LocalDateTime.now(UTC).minusMinutes(10)) // Expired
                    .used(false)
                    .build();

            when(tokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));

            // Act & Assert
            TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
                emailConfirmationService.validateTokenAndGenerateApiKey(token);
            });

            assertEquals("Token has expired", exception.getMessage());

            // Verify no API key was saved
            verify(apiKeyRepository, never()).save(any());
            verify(apiKeyRepository, never()).removeByEmail(any());
        }

        @Test
        @DisplayName("Should throw exception for already used token")
        void validateTokenAndGenerateApiKey_UsedToken() {
            // Arrange
            String token = UUID.randomUUID().toString();
            String email = "test@example.com";

            EmailConfirmTokenModel confirmToken = EmailConfirmTokenModel.builder()
                    .id(UUID.randomUUID())
                    .email(email)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .used(true) // Already used
                    .build();

            when(tokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));

            // Act & Assert
            TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
                emailConfirmationService.validateTokenAndGenerateApiKey(token);
            });

            assertEquals("Token has expired", exception.getMessage());

            // Verify no API key was saved
            verify(apiKeyRepository, never()).save(any());
            verify(apiKeyRepository, never()).removeByEmail(any());
        }

        @Test
        @DisplayName("Should handle token exactly at expiration boundary")
        void validateTokenAndGenerateApiKey_TokenAtExpirationBoundary() {
            // Arrange
            String token = UUID.randomUUID().toString();
            String email = "test@example.com";

            EmailConfirmTokenModel confirmToken = EmailConfirmTokenModel.builder()
                    .id(UUID.randomUUID())
                    .email(email)
                    .token(token)
                    .expiresAt(LocalDateTime.now(UTC)) // Exactly at expiration
                    .used(false)
                    .build();

            when(tokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));

            // Act & Assert
            TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
                emailConfirmationService.validateTokenAndGenerateApiKey(token);
            });

            assertEquals("Token has expired", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Generate API Key Tests")
    class GenerateApiKeyTests {

        @Test
        @DisplayName("Should generate valid API key with correct prefix")
        void generateApiKey_ReturnsValidKey() {
            // Act
            String apiKey = emailConfirmationService.generateApiKey("test@example.com");

            // Assert
            assertNotNull(apiKey);
            assertTrue(apiKey.startsWith("maps_live_"));
            assertEquals(34, apiKey.length());
        }

        @Test
        @DisplayName("Should generate unique API keys")
        void generateApiKey_GeneratesUniqueKeys() {
            // Act
            String key1 = emailConfirmationService.generateApiKey("test1@example.com");
            String key2 = emailConfirmationService.generateApiKey("test2@example.com");

            // Assert
            assertNotEquals(key1, key2);
            assertTrue(key1.startsWith("maps_live_"));
            assertTrue(key2.startsWith("maps_live_"));
        }

        @Test
        @DisplayName("Should generate alphanumeric API keys")
        void generateApiKey_AlphanumericOnly() {
            // Act
            String apiKey = emailConfirmationService.generateApiKey("test@example.com");

            // Assert
            String randomPart = apiKey.substring(10); // Remove "maps_live_" prefix
            assertTrue(randomPart.matches("[a-zA-Z0-9]+"), "API key should contain only alphanumeric characters");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null email gracefully")
        void handleNullEmail() {
            // Note: This would depend on validation annotations in the actual service
            // For now, we're testing the behavior as-is
            assertDoesNotThrow(() -> {
                emailConfirmationService.generateApiKey(null);
            });
        }

        @Test
        @DisplayName("Should handle empty email gracefully")
        void handleEmptyEmail() {
            String apiKey = emailConfirmationService.generateApiKey("");
            assertNotNull(apiKey);
            assertTrue(apiKey.startsWith("maps_live_"));
        }

        @Test
        @DisplayName("Should handle database errors during token save")
        void handleDatabaseErrors() {
            // Arrange
            String email = "test@example.com";
            when(apiKeyRepository.existsByEmailAndActive(email, true)).thenReturn(false);
            when(tokenRepository.save(any())).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                emailConfirmationService.generateAndSendConfirmationToken(email);
            });

            assertEquals("Database error", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should respect custom token expiration time")
        void customTokenExpirationTime() {
            // Arrange
            ReflectionTestUtils.setField(emailConfirmationService, "tokenExpirationMinutes", 30);
            String email = "test@example.com";
            when(apiKeyRepository.existsByEmailAndActive(email, true)).thenReturn(false);
            when(mailtrapService.sendTemplateEmail(eq(email), anyString(), anyString(), any(HashMap.class))).thenReturn(true);

            // Act
            emailConfirmationService.generateAndSendConfirmationToken(email);

            // Assert
            ArgumentCaptor<HashMap> templateModelCaptor = ArgumentCaptor.forClass(HashMap.class);
            verify(mailtrapService).sendTemplateEmail(
                    eq(email),
                    anyString(),
                    anyString(),
                    templateModelCaptor.capture()
            );

            HashMap<String, Object> templateModel = templateModelCaptor.getValue();
            assertEquals("in 30 minutes", templateModel.get("expirationTime"));
        }

        @Test
        @DisplayName("Should format expiration time correctly for hours")
        void formatExpirationTimeHours() {
            // Arrange
            ReflectionTestUtils.setField(emailConfirmationService, "tokenExpirationMinutes", 120);
            String email = "test@example.com";
            when(apiKeyRepository.existsByEmailAndActive(email, true)).thenReturn(false);
            when(mailtrapService.sendTemplateEmail(eq(email), anyString(), anyString(), any(HashMap.class))).thenReturn(true);

            // Act
            emailConfirmationService.generateAndSendConfirmationToken(email);

            // Assert
            ArgumentCaptor<HashMap> templateModelCaptor = ArgumentCaptor.forClass(HashMap.class);
            verify(mailtrapService).sendTemplateEmail(
                    eq(email),
                    anyString(),
                    anyString(),
                    templateModelCaptor.capture()
            );

            HashMap<String, Object> templateModel = templateModelCaptor.getValue();
            assertEquals("in 2 hours", templateModel.get("expirationTime"));
        }

        @Test
        @DisplayName("Should format expiration time correctly for single hour")
        void formatExpirationTimeSingleHour() {
            // Arrange
            ReflectionTestUtils.setField(emailConfirmationService, "tokenExpirationMinutes", 60);
            String email = "test@example.com";
            when(apiKeyRepository.existsByEmailAndActive(email, true)).thenReturn(false);
            when(mailtrapService.sendTemplateEmail(eq(email), anyString(), anyString(), any(HashMap.class))).thenReturn(true);

            // Act
            emailConfirmationService.generateAndSendConfirmationToken(email);

            // Assert
            ArgumentCaptor<HashMap> templateModelCaptor = ArgumentCaptor.forClass(HashMap.class);
            verify(mailtrapService).sendTemplateEmail(
                    eq(email),
                    anyString(),
                    anyString(),
                    templateModelCaptor.capture()
            );

            HashMap<String, Object> templateModel = templateModelCaptor.getValue();
            assertEquals("in 1 hour", templateModel.get("expirationTime"));
        }

        @Test
        @DisplayName("Should use correct base URL in confirmation link")
        void correctBaseUrlInConfirmationLink() {
            // Arrange
            ReflectionTestUtils.setField(emailConfirmationService, "baseUrl", "https://production.example.com");
            String email = "test@example.com";
            when(apiKeyRepository.existsByEmailAndActive(email, true)).thenReturn(false);
            when(mailtrapService.sendTemplateEmail(eq(email), anyString(), anyString(), any(HashMap.class))).thenReturn(true);

            // Act
            emailConfirmationService.generateAndSendConfirmationToken(email);

            // Assert
            ArgumentCaptor<HashMap> templateModelCaptor = ArgumentCaptor.forClass(HashMap.class);
            verify(mailtrapService).sendTemplateEmail(
                    eq(email),
                    anyString(),
                    anyString(),
                    templateModelCaptor.capture()
            );

            HashMap<String, Object> templateModel = templateModelCaptor.getValue();
            String confirmationUrl = (String) templateModel.get("confirmationUrl");
            assertTrue(confirmationUrl.startsWith("https://production.example.com"));
        }
    }
}