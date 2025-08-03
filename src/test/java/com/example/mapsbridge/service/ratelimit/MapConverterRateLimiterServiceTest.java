package com.example.mapsbridge.service.ratelimit;

import com.example.mapsbridge.exception.rate.ChatIdRateLimitExceededException;
import com.example.mapsbridge.exception.rate.EmailRateLimitExceededException;
import com.example.mapsbridge.exception.rate.IpRateLimitExceededException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MapConverterRateLimiterServiceTest {

    @Mock
    private RateLimiterRegistry rateLimiterRegistry;

    @Mock
    private RateLimiter rateLimiter;

    private MapConverterRateLimiterService mapConverterRateLimiterService;

    @BeforeEach
    public void setUp() {
        mapConverterRateLimiterService = new MapConverterRateLimiterService(rateLimiterRegistry);

        // Setup default behavior for the mock - use lenient to avoid unnecessary stubbing errors
        lenient().when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        lenient().when(rateLimiter.getRateLimiterConfig()).thenReturn(mock(io.github.resilience4j.ratelimiter.RateLimiterConfig.class));
    }

    @Test
    public void testCheckDailyQuotaForIp_Success() {
        // Given
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testIp = "192.168.1.1";

        // When & Then
        assertDoesNotThrow(() -> mapConverterRateLimiterService.checkDailyQuotaForIp(testIp));

        // Verify that the rate limiter was called with the prefixed identifier
        verify(rateLimiterRegistry).rateLimiter("daily_" + testIp, "geocodingIpConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckDailyQuotaForIp_ExceedsLimit() {
        // Given
        when(rateLimiter.acquirePermission()).thenReturn(false);
        String testIp = "192.168.1.1";

        // When & Then
        IpRateLimitExceededException exception = assertThrows(
                IpRateLimitExceededException.class,
                () -> mapConverterRateLimiterService.checkDailyQuotaForIp(testIp)
        );

        assertEquals(testIp, exception.getIp());
        verify(rateLimiterRegistry).rateLimiter("daily_" + testIp, "geocodingIpConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckDailyQuotaForIp_ReusesSameRateLimiterForSameIp() {
        // Given
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testIp = "192.168.1.1";

        // When
        mapConverterRateLimiterService.checkDailyQuotaForIp(testIp);
        mapConverterRateLimiterService.checkDailyQuotaForIp(testIp);

        // Then - should only create rate limiter once for the same IP
        verify(rateLimiterRegistry, times(1)).rateLimiter("daily_" + testIp, "geocodingIpConfig");
        verify(rateLimiter, times(2)).acquirePermission();
    }

    @Test
    public void testCheckDailyQuotaForIp_HandlesException() {
        // Given
        when(rateLimiter.acquirePermission()).thenThrow(new RuntimeException("Test exception"));
        String testIp = "192.168.1.1";

        // When & Then - should not throw exception, allowing request to proceed
        assertDoesNotThrow(() -> mapConverterRateLimiterService.checkDailyQuotaForIp(testIp));
    }

    @Test
    public void testUpdatesLastAccessTimeForIp() {
        // Given
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testIp = "192.168.1.1";

        // When
        mapConverterRateLimiterService.checkDailyQuotaForIp(testIp);

        // Then - verify that last access time was updated
        Map<String, LocalDateTime> ipLastAccessTimes = (Map<String, LocalDateTime>)
                ReflectionTestUtils.getField(mapConverterRateLimiterService, "ipLastAccessTimes");

        assertNotNull(ipLastAccessTimes);
        assertTrue(ipLastAccessTimes.containsKey("daily_" + testIp));
        assertNotNull(ipLastAccessTimes.get("daily_" + testIp));
    }

    @Test
    public void testCleanupOldIpRateLimiters() {
        // Setup test data
        Map<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();
        Map<String, LocalDateTime> ipLastAccessTimes = new ConcurrentHashMap<>();

        // Add some test entries with different access times
        ipRateLimiters.put("192.168.1.1", rateLimiter);
        ipRateLimiters.put("192.168.1.2", rateLimiter);
        ipRateLimiters.put("192.168.1.3", rateLimiter);

        // Set access times: one recent, two old (using UTC to match the service implementation)
        ipLastAccessTimes.put("192.168.1.1", LocalDateTime.now(UTC));
        ipLastAccessTimes.put("192.168.1.2", LocalDateTime.now(UTC).minusHours(25));
        ipLastAccessTimes.put("192.168.1.3", LocalDateTime.now(UTC).minusHours(30));

        // Use reflection to set the private fields
        ReflectionTestUtils.setField(mapConverterRateLimiterService, "ipRateLimiters", ipRateLimiters);
        ReflectionTestUtils.setField(mapConverterRateLimiterService, "ipLastAccessTimes", ipLastAccessTimes);

        // Execute cleanup with 24 hours threshold
        int removedCount = mapConverterRateLimiterService.cleanupOldIpRateLimiters(24);

        // Verify results
        assertEquals(2, removedCount, "Should have removed 2 old entries");
        assertEquals(1, ipRateLimiters.size(), "Should have 1 entry left");
        assertEquals(1, ipLastAccessTimes.size(), "Should have 1 entry left");
        assertTrue(ipRateLimiters.containsKey("192.168.1.1"), "Recent entry should remain");
    }

    @Test
    public void testCheckDailyQuotaForEmail_Success() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testEmail = "test@example.com";

        // When & Then
        assertDoesNotThrow(() -> mapConverterRateLimiterService.checkDailyQuotaForEmail(testEmail));

        // Verify that the rate limiter was called with the prefixed identifier
        verify(rateLimiterRegistry).rateLimiter("daily_" + testEmail, "geocodingEmailConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckDailyQuotaForEmail_ExceedsLimit() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(false);
        String testEmail = "test@example.com";

        // When & Then
        EmailRateLimitExceededException exception = assertThrows(
                EmailRateLimitExceededException.class,
                () -> mapConverterRateLimiterService.checkDailyQuotaForEmail(testEmail)
        );

        assertEquals(testEmail, exception.getEmail());
        verify(rateLimiterRegistry).rateLimiter("daily_" + testEmail, "geocodingEmailConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckDailyQuotaForEmail_ReusesSameRateLimiterForSameEmail() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testEmail = "test@example.com";

        // When
        mapConverterRateLimiterService.checkDailyQuotaForEmail(testEmail);
        mapConverterRateLimiterService.checkDailyQuotaForEmail(testEmail);

        // Then - should only create rate limiter once for the same email
        verify(rateLimiterRegistry, times(1)).rateLimiter("daily_" + testEmail, "geocodingEmailConfig");
        verify(rateLimiter, times(2)).acquirePermission();
    }

    @Test
    public void testCheckDailyQuotaForEmail_HandlesException() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenThrow(new RuntimeException("Test exception"));
        String testEmail = "test@example.com";

        // When & Then - should not throw exception, allowing request to proceed
        assertDoesNotThrow(() -> mapConverterRateLimiterService.checkDailyQuotaForEmail(testEmail));
    }

    @Test
    public void testUpdatesLastAccessTimeForEmail() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testEmail = "test@example.com";

        // When
        mapConverterRateLimiterService.checkDailyQuotaForEmail(testEmail);

        // Then - verify that last access time was updated
        Map<String, LocalDateTime> emailLastAccessTimes = (Map<String, LocalDateTime>)
                ReflectionTestUtils.getField(mapConverterRateLimiterService, "emailLastAccessTimes");

        assertNotNull(emailLastAccessTimes);
        assertTrue(emailLastAccessTimes.containsKey("daily_" + testEmail));
        assertNotNull(emailLastAccessTimes.get("daily_" + testEmail));
    }

    @Test
    public void testCleanupOldEmailRateLimiters() {
        // Setup test data
        Map<String, RateLimiter> emailRateLimiters = new ConcurrentHashMap<>();
        Map<String, LocalDateTime> emailLastAccessTimes = new ConcurrentHashMap<>();

        // Add some test entries with different access times
        emailRateLimiters.put("user1@example.com", rateLimiter);
        emailRateLimiters.put("user2@example.com", rateLimiter);
        emailRateLimiters.put("user3@example.com", rateLimiter);

        // Set access times: one recent, two old (using UTC to match the service implementation)
        emailLastAccessTimes.put("user1@example.com", LocalDateTime.now(UTC));
        emailLastAccessTimes.put("user2@example.com", LocalDateTime.now(UTC).minusHours(25));
        emailLastAccessTimes.put("user3@example.com", LocalDateTime.now(UTC).minusHours(30));

        // Use reflection to set the private fields
        ReflectionTestUtils.setField(mapConverterRateLimiterService, "emailRateLimiters", emailRateLimiters);
        ReflectionTestUtils.setField(mapConverterRateLimiterService, "emailLastAccessTimes", emailLastAccessTimes);

        // Execute cleanup with 24 hours threshold
        int removedCount = mapConverterRateLimiterService.cleanupOldEmailRateLimiters(24);

        // Verify results
        assertEquals(2, removedCount, "Should have removed 2 old entries");
        assertEquals(1, emailRateLimiters.size(), "Should have 1 entry left");
        assertEquals(1, emailLastAccessTimes.size(), "Should have 1 entry left");
        assertTrue(emailRateLimiters.containsKey("user1@example.com"), "Recent entry should remain");
    }

    @Test
    public void testCheckDailyQuotaForChatId_Success() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testChatId = "123456789";

        // When & Then
        assertDoesNotThrow(() -> mapConverterRateLimiterService.checkDailyQuotaForChatId(testChatId));

        // Verify that the rate limiter was called with the prefixed identifier
        verify(rateLimiterRegistry).rateLimiter("daily_" + testChatId, "geocodingChatIdConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckDailyQuotaForChatId_ExceedsLimit() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(false);
        String testChatId = "123456789";

        // When & Then
        ChatIdRateLimitExceededException exception = assertThrows(
                ChatIdRateLimitExceededException.class,
                () -> mapConverterRateLimiterService.checkDailyQuotaForChatId(testChatId)
        );

        assertEquals(testChatId, exception.getChatId());
        verify(rateLimiterRegistry).rateLimiter("daily_" + testChatId, "geocodingChatIdConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckDailyQuotaForChatId_ReusesSameRateLimiterForSameChatId() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testChatId = "123456789";

        // When
        mapConverterRateLimiterService.checkDailyQuotaForChatId(testChatId);
        mapConverterRateLimiterService.checkDailyQuotaForChatId(testChatId);

        // Then - should only create rate limiter once for the same chat ID
        verify(rateLimiterRegistry, times(1)).rateLimiter("daily_" + testChatId, "geocodingChatIdConfig");
        verify(rateLimiter, times(2)).acquirePermission();
    }

    @Test
    public void testCheckDailyQuotaForChatId_HandlesException() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenThrow(new RuntimeException("Test exception"));
        String testChatId = "123456789";

        // When & Then - should not throw exception, allowing request to proceed
        assertDoesNotThrow(() -> mapConverterRateLimiterService.checkDailyQuotaForChatId(testChatId));
    }

    @Test
    public void testUpdatesLastAccessTimeForChatId() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testChatId = "123456789";

        // When
        mapConverterRateLimiterService.checkDailyQuotaForChatId(testChatId);

        // Then - verify that last access time was updated
        Map<String, LocalDateTime> chatIdLastAccessTimes = (Map<String, LocalDateTime>)
                ReflectionTestUtils.getField(mapConverterRateLimiterService, "chatIdLastAccessTimes");

        assertNotNull(chatIdLastAccessTimes);
        assertTrue(chatIdLastAccessTimes.containsKey("daily_" + testChatId));
        assertNotNull(chatIdLastAccessTimes.get("daily_" + testChatId));
    }

    @Test
    public void testCleanupOldChatIdRateLimiters() {
        // Setup test data
        Map<String, RateLimiter> chatIdRateLimiters = new ConcurrentHashMap<>();
        Map<String, LocalDateTime> chatIdLastAccessTimes = new ConcurrentHashMap<>();

        // Add some test entries with different access times
        chatIdRateLimiters.put("123456789", rateLimiter);
        chatIdRateLimiters.put("987654321", rateLimiter);
        chatIdRateLimiters.put("555555555", rateLimiter);

        // Set access times: one recent, two old (using UTC to match the service implementation)
        chatIdLastAccessTimes.put("123456789", LocalDateTime.now(UTC));
        chatIdLastAccessTimes.put("987654321", LocalDateTime.now(UTC).minusHours(25));
        chatIdLastAccessTimes.put("555555555", LocalDateTime.now(UTC).minusHours(30));

        // Use reflection to set the private fields
        ReflectionTestUtils.setField(mapConverterRateLimiterService, "chatIdRateLimiters", chatIdRateLimiters);
        ReflectionTestUtils.setField(mapConverterRateLimiterService, "chatIdLastAccessTimes", chatIdLastAccessTimes);

        // Execute cleanup with 24 hours threshold
        int removedCount = mapConverterRateLimiterService.cleanupOldChatIdRateLimiters(24);

        // Verify results
        assertEquals(2, removedCount, "Should have removed 2 old entries");
        assertEquals(1, chatIdRateLimiters.size(), "Should have 1 entry left");
        assertEquals(1, chatIdLastAccessTimes.size(), "Should have 1 entry left");
        assertTrue(chatIdRateLimiters.containsKey("123456789"), "Recent entry should remain");
    }
}