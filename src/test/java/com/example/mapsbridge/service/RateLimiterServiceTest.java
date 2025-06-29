package com.example.mapsbridge.service;

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
public class RateLimiterServiceTest {

    @Mock
    private RateLimiterRegistry rateLimiterRegistry;

    @Mock
    private RateLimiter rateLimiter;

    private RateLimiterService rateLimiterService;

    @BeforeEach
    public void setUp() {
        rateLimiterService = new RateLimiterService(rateLimiterRegistry);
    }

    @Test
    public void testCheckRateLimit_Success() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testIp = "192.168.1.1";

        // When & Then
        assertDoesNotThrow(() -> rateLimiterService.checkRateLimit(testIp));

        // Verify that the rate limiter was called
        verify(rateLimiterRegistry).rateLimiter(testIp, "ipConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckRateLimit_ExceedsLimit() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(false);
        String testIp = "192.168.1.1";

        // When & Then
        IpRateLimitExceededException exception = assertThrows(
                IpRateLimitExceededException.class,
                () -> rateLimiterService.checkRateLimit(testIp)
        );

        assertEquals(testIp, exception.getIp());
        verify(rateLimiterRegistry).rateLimiter(testIp, "ipConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckRateLimit_ReusesSameRateLimiterForSameIp() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testIp = "192.168.1.1";

        // When
        rateLimiterService.checkRateLimit(testIp);
        rateLimiterService.checkRateLimit(testIp);

        // Then - should only create rate limiter once for the same IP
        verify(rateLimiterRegistry, times(1)).rateLimiter(testIp, "ipConfig");
        verify(rateLimiter, times(2)).acquirePermission();
    }

    @Test
    public void testCheckRateLimitForEmail_Success() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testEmail = "test@example.com";

        // When & Then
        assertDoesNotThrow(() -> rateLimiterService.checkRateLimitForEmail(testEmail));

        // Verify that the rate limiter was called
        verify(rateLimiterRegistry).rateLimiter(testEmail, "emailConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckRateLimitForEmail_ExceedsLimit() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(false);
        String testEmail = "test@example.com";

        // When & Then
        EmailRateLimitExceededException exception = assertThrows(
                EmailRateLimitExceededException.class,
                () -> rateLimiterService.checkRateLimitForEmail(testEmail)
        );

        assertEquals(testEmail, exception.getEmail());
        verify(rateLimiterRegistry).rateLimiter(testEmail, "emailConfig");
        verify(rateLimiter).acquirePermission();
    }

    @Test
    public void testCheckRateLimitForEmail_ReusesSameRateLimiterForSameEmail() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testEmail = "test@example.com";

        // When
        rateLimiterService.checkRateLimitForEmail(testEmail);
        rateLimiterService.checkRateLimitForEmail(testEmail);

        // Then - should only create rate limiter once for the same email
        verify(rateLimiterRegistry, times(1)).rateLimiter(testEmail, "emailConfig");
        verify(rateLimiter, times(2)).acquirePermission();
    }

    @Test
    public void testCheckRateLimit_HandlesException() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenThrow(new RuntimeException("Test exception"));
        String testIp = "192.168.1.1";

        // When & Then - should not throw exception, allowing request to proceed
        assertDoesNotThrow(() -> rateLimiterService.checkRateLimit(testIp));
    }

    @Test
    public void testCheckRateLimitForEmail_HandlesException() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenThrow(new RuntimeException("Test exception"));
        String testEmail = "test@example.com";

        // When & Then - should not throw exception, allowing request to proceed
        assertDoesNotThrow(() -> rateLimiterService.checkRateLimitForEmail(testEmail));
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
        ReflectionTestUtils.setField(rateLimiterService, "ipRateLimiters", ipRateLimiters);
        ReflectionTestUtils.setField(rateLimiterService, "ipLastAccessTimes", ipLastAccessTimes);

        // Execute cleanup with 24 hours threshold
        int removedCount = rateLimiterService.cleanupOldIpRateLimiters(24);

        // Verify results
        assertEquals(2, removedCount, "Should have removed 2 old entries");
        assertEquals(1, ipRateLimiters.size(), "Should have 1 entry left");
        assertEquals(1, ipLastAccessTimes.size(), "Should have 1 entry left");
        assertTrue(ipRateLimiters.containsKey("192.168.1.1"), "Recent entry should remain");
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

        // Set access times: one recent, two old
        emailLastAccessTimes.put("user1@example.com", LocalDateTime.now(UTC));
        emailLastAccessTimes.put("user2@example.com", LocalDateTime.now(UTC).minusHours(25));
        emailLastAccessTimes.put("user3@example.com", LocalDateTime.now(UTC).minusHours(30));

        // Use reflection to set the private fields
        ReflectionTestUtils.setField(rateLimiterService, "emailRateLimiters", emailRateLimiters);
        ReflectionTestUtils.setField(rateLimiterService, "emailLastAccessTimes", emailLastAccessTimes);

        // Execute cleanup with 24 hours threshold
        int removedCount = rateLimiterService.cleanupOldEmailRateLimiters(24);

        // Verify results
        assertEquals(2, removedCount, "Should have removed 2 old entries");
        assertEquals(1, emailRateLimiters.size(), "Should have 1 entry left");
        assertEquals(1, emailLastAccessTimes.size(), "Should have 1 entry left");
        assertTrue(emailRateLimiters.containsKey("user1@example.com"), "Recent entry should remain");
    }

    @Test
    public void testCleanupOldIpRateLimiters_NoOldEntries() {
        // Setup test data with all recent entries
        Map<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();
        Map<String, LocalDateTime> ipLastAccessTimes = new ConcurrentHashMap<>();

        ipRateLimiters.put("192.168.1.1", rateLimiter);
        ipRateLimiters.put("192.168.1.2", rateLimiter);

        ipLastAccessTimes.put("192.168.1.1", LocalDateTime.now());
        ipLastAccessTimes.put("192.168.1.2", LocalDateTime.now().minusHours(1));

        ReflectionTestUtils.setField(rateLimiterService, "ipRateLimiters", ipRateLimiters);
        ReflectionTestUtils.setField(rateLimiterService, "ipLastAccessTimes", ipLastAccessTimes);

        // Execute cleanup with 24 hours threshold
        int removedCount = rateLimiterService.cleanupOldIpRateLimiters(24);

        // Verify no entries were removed
        assertEquals(0, removedCount, "Should have removed 0 entries");
        assertEquals(2, ipRateLimiters.size(), "Should have 2 entries left");
        assertEquals(2, ipLastAccessTimes.size(), "Should have 2 entries left");
    }

    @Test
    public void testCleanupOldEmailRateLimiters_EmptyMaps() {
        // Setup empty maps
        Map<String, RateLimiter> emailRateLimiters = new ConcurrentHashMap<>();
        Map<String, LocalDateTime> emailLastAccessTimes = new ConcurrentHashMap<>();

        ReflectionTestUtils.setField(rateLimiterService, "emailRateLimiters", emailRateLimiters);
        ReflectionTestUtils.setField(rateLimiterService, "emailLastAccessTimes", emailLastAccessTimes);

        // Execute cleanup
        int removedCount = rateLimiterService.cleanupOldEmailRateLimiters(24);

        // Verify no entries were removed
        assertEquals(0, removedCount, "Should have removed 0 entries");
        assertEquals(0, emailRateLimiters.size(), "Should have 0 entries");
        assertEquals(0, emailLastAccessTimes.size(), "Should have 0 entries");
    }

    @Test
    public void testUpdatesLastAccessTimeForIp() {
        // Given
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testIp = "192.168.1.1";

        // When
        rateLimiterService.checkRateLimit(testIp);

        // Then - verify that last access time was updated
        Map<String, LocalDateTime> ipLastAccessTimes = (Map<String, LocalDateTime>)
                ReflectionTestUtils.getField(rateLimiterService, "ipLastAccessTimes");

        assertNotNull(ipLastAccessTimes);
        assertTrue(ipLastAccessTimes.containsKey(testIp));
        assertNotNull(ipLastAccessTimes.get(testIp));
    }

    @Test
    public void testUpdatesLastAccessTimeForEmail() {
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(rateLimiter);
        // Given
        when(rateLimiter.acquirePermission()).thenReturn(true);
        String testEmail = "test@example.com";

        // When
        rateLimiterService.checkRateLimitForEmail(testEmail);

        // Then - verify that last access time was updated
        Map<String, LocalDateTime> emailLastAccessTimes = (Map<String, LocalDateTime>)
                ReflectionTestUtils.getField(rateLimiterService, "emailLastAccessTimes");

        assertNotNull(emailLastAccessTimes);
        assertTrue(emailLastAccessTimes.containsKey(testEmail));
        assertNotNull(emailLastAccessTimes.get(testEmail));
    }
}