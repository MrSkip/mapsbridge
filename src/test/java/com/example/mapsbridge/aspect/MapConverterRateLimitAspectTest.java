package com.example.mapsbridge.aspect;

import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.config.metrics.tracker.ClientTracker;
import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.request.ConvertRequest;
import com.example.mapsbridge.dto.response.WebConvertResponse;
import com.example.mapsbridge.exception.rate.ChatIdRateLimitExceededException;
import com.example.mapsbridge.exception.rate.EmailRateLimitExceededException;
import com.example.mapsbridge.exception.rate.IpRateLimitExceededException;
import com.example.mapsbridge.service.converter.MapConverterService;
import com.example.mapsbridge.service.ratelimit.MapConverterRateLimiterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MapConverterRateLimitAspectTest {

    @Mock
    private MapConverterRateLimiterService mapConverterRateLimiterService;
    @Mock
    private MapConverterService<WebConvertResponse> mapConverterService;
    @Mock
    private ClientTracker clientTracker;

    @InjectMocks
    private MapConverterRateLimitAspect aspect;

    private MapConverterService<WebConvertResponse> proxiedMapConverterService;

    @BeforeEach
    public void setUp() {
        // Create a proxy with the aspect
        AspectJProxyFactory factory = new AspectJProxyFactory(mapConverterService);
        factory.addAspect(aspect);
        proxiedMapConverterService = factory.getProxy();
    }

    @AfterEach
    public void tearDown() {
        // Clear the LoggingContext after each test
        LoggingContext.clear();
    }

    @Test
    public void testConvert_WithIpAddress() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        WebConvertResponse expectedResponse = new WebConvertResponse();
        expectedResponse.setCoordinates(new Coordinate(1.0, 2.0));
        expectedResponse.setName("Test Location");
        String ipAddress = "192.168.1.1";

        // Set up the LoggingContext
        LoggingContext.setIpAddress(ipAddress);

        // Set up the mocks
        when(mapConverterService.convert(request)).thenReturn(expectedResponse);

        // When
        WebConvertResponse result = proxiedMapConverterService.convert(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(mapConverterRateLimiterService).checkDailyQuotaForIp(ipAddress);
        verify(mapConverterService).convert(request);
    }

    @Test
    public void testConvert_WithChatId() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        WebConvertResponse expectedResponse = new WebConvertResponse();
        expectedResponse.setCoordinates(new Coordinate(1.0, 2.0));
        expectedResponse.setName("Test Location");
        String chatId = "123456789";

        // Set up the LoggingContext
        LoggingContext.setChatId(chatId);

        // Set up the mocks
        when(mapConverterService.convert(request)).thenReturn(expectedResponse);

        // When
        WebConvertResponse result = proxiedMapConverterService.convert(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(mapConverterRateLimiterService).checkDailyQuotaForChatId(chatId);
        verify(mapConverterService).convert(request);
    }

    @Test
    public void testConvert_WithEmail() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        WebConvertResponse expectedResponse = new WebConvertResponse();
        expectedResponse.setCoordinates(new Coordinate(1.0, 2.0));
        expectedResponse.setName("Test Location");
        String email = "test@example.com";

        // Set up the LoggingContext
        LoggingContext.setEmail(email);

        // Set up the mocks
        when(mapConverterService.convert(request)).thenReturn(expectedResponse);

        // When
        WebConvertResponse result = proxiedMapConverterService.convert(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(mapConverterRateLimiterService).checkDailyQuotaForEmail(email);
        verify(mapConverterService).convert(request);
    }

    @Test
    public void testConvert_WithAllIdentifiers() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        WebConvertResponse expectedResponse = new WebConvertResponse();
        expectedResponse.setCoordinates(new Coordinate(1.0, 2.0));
        expectedResponse.setName("Test Location");
        String ipAddress = "192.168.1.1";
        String chatId = "123456789";
        String email = "test@example.com";

        // Set up the LoggingContext
        LoggingContext.setIpAddress(ipAddress);
        LoggingContext.setChatId(chatId);
        LoggingContext.setEmail(email);

        // Set up the mocks
        when(mapConverterService.convert(request)).thenReturn(expectedResponse);

        // When
        WebConvertResponse result = proxiedMapConverterService.convert(request);

        // Then
        assertEquals(expectedResponse, result);
        // Only email rate limit should be checked when all identifiers are present (priority-based)
        verify(mapConverterRateLimiterService).checkDailyQuotaForEmail(email);
        verify(mapConverterRateLimiterService, never()).checkDailyQuotaForIp(ipAddress);
        verify(mapConverterRateLimiterService, never()).checkDailyQuotaForChatId(chatId);
        verify(mapConverterService).convert(request);
    }

    @Test
    public void testConvert_IpRateLimitExceeded() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        String ipAddress = "192.168.1.1";

        // Set up the LoggingContext
        LoggingContext.setIpAddress(ipAddress);

        // Set up the mocks
        doThrow(new IpRateLimitExceededException(ipAddress))
                .when(mapConverterRateLimiterService).checkDailyQuotaForIp(ipAddress);

        // When & Then
        assertThrows(IpRateLimitExceededException.class, () -> {
            proxiedMapConverterService.convert(request);
        });

        verify(mapConverterRateLimiterService).checkDailyQuotaForIp(ipAddress);
        verify(mapConverterService, never()).convert(request);
    }

    @Test
    public void testConvert_ChatIdRateLimitExceeded() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        String chatId = "123456789";

        // Set up the LoggingContext
        LoggingContext.setChatId(chatId);

        // Set up the mocks
        doThrow(new ChatIdRateLimitExceededException(chatId))
                .when(mapConverterRateLimiterService).checkDailyQuotaForChatId(chatId);

        // When & Then
        assertThrows(ChatIdRateLimitExceededException.class, () -> {
            proxiedMapConverterService.convert(request);
        });

        verify(mapConverterRateLimiterService).checkDailyQuotaForChatId(chatId);
        verify(mapConverterService, never()).convert(request);
    }

    @Test
    public void testConvert_EmailRateLimitExceeded() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        String email = "test@example.com";

        // Set up the LoggingContext
        LoggingContext.setEmail(email);

        // Set up the mocks
        doThrow(new EmailRateLimitExceededException(email))
                .when(mapConverterRateLimiterService).checkDailyQuotaForEmail(email);

        // When & Then
        assertThrows(EmailRateLimitExceededException.class, () -> {
            proxiedMapConverterService.convert(request);
        });

        verify(mapConverterRateLimiterService).checkDailyQuotaForEmail(email);
        verify(mapConverterService, never()).convert(request);
    }

    @Test
    public void testConvert_WithIpAndChatId() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        WebConvertResponse expectedResponse = new WebConvertResponse();
        expectedResponse.setCoordinates(new Coordinate(1.0, 2.0));
        expectedResponse.setName("Test Location");
        String ipAddress = "192.168.1.1";
        String chatId = "123456789";

        // Set up the LoggingContext with IP and chatId but no email
        LoggingContext.setIpAddress(ipAddress);
        LoggingContext.setChatId(chatId);
        LoggingContext.setEmail(null);

        // Set up the mocks
        when(mapConverterService.convert(request)).thenReturn(expectedResponse);

        // When
        WebConvertResponse result = proxiedMapConverterService.convert(request);

        // Then
        assertEquals(expectedResponse, result);
        // Only IP rate limit should be checked when email is not present but IP is present
        verify(mapConverterRateLimiterService).checkDailyQuotaForIp(ipAddress);
        verify(mapConverterRateLimiterService, never()).checkDailyQuotaForEmail(anyString());
        verify(mapConverterRateLimiterService, never()).checkDailyQuotaForChatId(chatId);
        verify(mapConverterService).convert(request);
    }

    @Test
    public void testConvert_WithChatIdOnly() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        WebConvertResponse expectedResponse = new WebConvertResponse();
        expectedResponse.setCoordinates(new Coordinate(1.0, 2.0));
        expectedResponse.setName("Test Location");
        String chatId = "123456789";

        // Set up the LoggingContext with only chatId
        LoggingContext.setIpAddress(null);
        LoggingContext.setChatId(chatId);
        LoggingContext.setEmail(null);

        // Set up the mocks
        when(mapConverterService.convert(request)).thenReturn(expectedResponse);

        // When
        WebConvertResponse result = proxiedMapConverterService.convert(request);

        // Then
        assertEquals(expectedResponse, result);
        // Only chatId rate limit should be checked when neither email nor IP is present
        verify(mapConverterRateLimiterService).checkDailyQuotaForChatId(chatId);
        verify(mapConverterRateLimiterService, never()).checkDailyQuotaForEmail(anyString());
        verify(mapConverterRateLimiterService, never()).checkDailyQuotaForIp(anyString());
        verify(mapConverterService).convert(request);
    }

    @Test
    public void testConvert_WithIpAddress_SdkEndpoint() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        WebConvertResponse expectedResponse = new WebConvertResponse();
        expectedResponse.setCoordinates(new Coordinate(1.0, 2.0));
        expectedResponse.setName("Test Location");
        String ipAddress = "192.168.1.1";

        // Set up the LoggingContext
        LoggingContext.setIpAddress(ipAddress);
        LoggingContext.setEndpointType("sdk");

        // Set up the mocks
        when(mapConverterService.convert(request)).thenReturn(expectedResponse);

        // When
        WebConvertResponse result = proxiedMapConverterService.convert(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(mapConverterRateLimiterService).checkDailyQuotaForIp(ipAddress);
        verify(clientTracker).trackSdkRequest();
        verify(clientTracker, never()).trackWebRequest();
        verify(clientTracker, never()).trackShortcutRequest();
        verify(mapConverterService).convert(request);
    }

    @Test
    public void testConvert_WithIpAddress_ShortcutEndpoint() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        WebConvertResponse expectedResponse = new WebConvertResponse();
        expectedResponse.setCoordinates(new Coordinate(1.0, 2.0));
        expectedResponse.setName("Test Location");
        String ipAddress = "192.168.1.1";

        // Set up the LoggingContext
        LoggingContext.setIpAddress(ipAddress);
        LoggingContext.setEndpointType("shortcut");

        // Set up the mocks
        when(mapConverterService.convert(request)).thenReturn(expectedResponse);

        // When
        WebConvertResponse result = proxiedMapConverterService.convert(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(mapConverterRateLimiterService).checkDailyQuotaForIp(ipAddress);
        verify(clientTracker).trackShortcutRequest();
        verify(clientTracker, never()).trackWebRequest();
        verify(clientTracker, never()).trackSdkRequest();
        verify(mapConverterService).convert(request);
    }

    @Test
    public void testConvert_WithIpAddress_WebEndpoint() {
        // Given
        ConvertRequest request = new ConvertRequest("1.0,2.0");
        WebConvertResponse expectedResponse = new WebConvertResponse();
        expectedResponse.setCoordinates(new Coordinate(1.0, 2.0));
        expectedResponse.setName("Test Location");
        String ipAddress = "192.168.1.1";

        // Set up the LoggingContext
        LoggingContext.setIpAddress(ipAddress);
        LoggingContext.setEndpointType("web");

        // Set up the mocks
        when(mapConverterService.convert(request)).thenReturn(expectedResponse);

        // When
        WebConvertResponse result = proxiedMapConverterService.convert(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(mapConverterRateLimiterService).checkDailyQuotaForIp(ipAddress);
        verify(clientTracker).trackWebRequest();
        verify(clientTracker, never()).trackSdkRequest();
        verify(clientTracker, never()).trackShortcutRequest();
        verify(mapConverterService).convert(request);
    }
}