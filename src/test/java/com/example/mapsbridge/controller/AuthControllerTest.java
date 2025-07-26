package com.example.mapsbridge.controller;

import com.example.mapsbridge.dto.ApiKeyResponseDto;
import com.example.mapsbridge.dto.KeyRequestDto;
import com.example.mapsbridge.dto.MessageResponseDto;
import com.example.mapsbridge.exception.InvalidTokenException;
import com.example.mapsbridge.service.EmailConfirmationService;
import com.example.mapsbridge.service.MailtrapService;
import com.example.mapsbridge.service.ratelimit.UserRateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
class AuthControllerTest {

    @MockitoBean
    private EmailConfirmationService emailConfirmationService;

    @MockitoBean
    private UserRateLimiterService userRateLimiterService;

    @MockitoBean
    private MailtrapService mailtrapService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRequestApiKey_Success() throws Exception {
        // Given
        KeyRequestDto request = new KeyRequestDto();
        request.setEmail("test@example.com");

        MessageResponseDto response = MessageResponseDto.builder()
                .message("Confirmation email sent. Please check your inbox.")
                .success(true)
                .build();

        when(emailConfirmationService.generateAndSendConfirmationToken(anyString())).thenReturn(response);
        doNothing().when(userRateLimiterService).checkIpRateLimit();
        doNothing().when(userRateLimiterService).checkRateLimitForEmail(anyString());

        // When/Then
        mockMvc.perform(post("/auth/api/request-api-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Confirmation email sent. Please check your inbox."));

        // Verify service methods were called
        verify(userRateLimiterService).checkIpRateLimit();
        verify(userRateLimiterService).checkRateLimitForEmail("test@example.com");
        verify(emailConfirmationService).generateAndSendConfirmationToken("test@example.com");
    }

    @Test
    void testConfirmEmail_Success() throws Exception {
        // Given
        String token = "valid-token";
        ApiKeyResponseDto response = ApiKeyResponseDto.builder()
                .apiKey("maps_live_abc123")
                .build();

        when(emailConfirmationService.validateTokenAndGenerateApiKey(token)).thenReturn(response);
        doNothing().when(userRateLimiterService).checkIpRateLimit();

        // When/Then
        mockMvc.perform(get("/auth/api/confirm")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").value("maps_live_abc123"));

        // Verify service methods were called
        verify(userRateLimiterService).checkIpRateLimit();
        verify(emailConfirmationService).validateTokenAndGenerateApiKey(token);
    }

    @Test
    void testRequestApiKey_InvalidEmail() throws Exception {
        // Given
        KeyRequestDto request = new KeyRequestDto();
        request.setEmail("invalid-email"); // Invalid email format

        // When/Then
        mockMvc.perform(post("/auth/api/request-api-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        // Verify no service methods were called
        verify(emailConfirmationService, never()).generateAndSendConfirmationToken(anyString());
    }

    @Test
    void testConfirmEmail_InvalidToken() throws Exception {
        // Given
        String token = "invalid-token";

        when(emailConfirmationService.validateTokenAndGenerateApiKey(token))
                .thenThrow(new InvalidTokenException("Invalid token"));
        doNothing().when(userRateLimiterService).checkIpRateLimit();

        // When/Then
        mockMvc.perform(get("/auth/api/confirm")
                        .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid token"));

        // Verify service methods were called
        verify(userRateLimiterService).checkIpRateLimit();
        verify(emailConfirmationService).validateTokenAndGenerateApiKey(token);
    }

}