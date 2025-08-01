package com.example.mapsbridge.metrics;

import com.example.mapsbridge.config.metrics.tracker.IpAddressTracker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IpAddressTrackerTest {

    @Mock
    private MeterRegistry meterRegistry;

    private IpAddressTracker ipAddressTracker;

    @BeforeEach
    public void setUp() {
        // Create a real IpAddressTracker with the mock MeterRegistry
        ipAddressTracker = new IpAddressTracker(meterRegistry);

        // Create a spy to intercept the incrementCounter method
        ipAddressTracker = spy(ipAddressTracker);

        // Mock the incrementCounter method to avoid actual counter operations
        // Use lenient to avoid unnecessary stubbing exceptions
        Mockito.lenient().doNothing().when(ipAddressTracker).incrementCounter();
    }

    @Test
    public void testTrackIpAddress_UniqueIp_ShouldIncrementCounter() {
        // Given
        String ipAddress = "192.168.1.1";

        // When
        ipAddressTracker.trackIpAddress(ipAddress);

        // Then
        verify(ipAddressTracker, times(1)).incrementCounter();
        assertEquals(1, ipAddressTracker.getUniqueIpCountForToday());
    }

    @Test
    public void testTrackIpAddress_SameIpTwice_ShouldIncrementCounterOnce() {
        // Given
        String ipAddress = "192.168.1.1";

        // When
        ipAddressTracker.trackIpAddress(ipAddress);
        ipAddressTracker.trackIpAddress(ipAddress);

        // Then
        verify(ipAddressTracker, times(1)).incrementCounter();
        assertEquals(1, ipAddressTracker.getUniqueIpCountForToday());
    }

    @Test
    public void testTrackIpAddress_MultipleUniqueIps_ShouldIncrementCounterForEach() {
        // Given
        String ipAddress1 = "192.168.1.1";
        String ipAddress2 = "192.168.1.2";
        String ipAddress3 = "192.168.1.3";

        // When
        ipAddressTracker.trackIpAddress(ipAddress1);
        ipAddressTracker.trackIpAddress(ipAddress2);
        ipAddressTracker.trackIpAddress(ipAddress3);

        // Then
        verify(ipAddressTracker, times(3)).incrementCounter();
        assertEquals(3, ipAddressTracker.getUniqueIpCountForToday());
    }


    // Integration test with a real registry
    @Test
    public void testIntegrationWithRealRegistry() {
        // Given
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        // Create a new tracker instance that's not a spy
        IpAddressTracker tracker = new IpAddressTracker(registry);

        // When
        tracker.trackIpAddress("192.168.1.1");
        tracker.trackIpAddress("192.168.1.2");
        tracker.trackIpAddress("192.168.1.1"); // Duplicate

        // Then
        assertEquals(2, tracker.getUniqueIpCountForToday());

        // Verify the counter in the registry
        double count = registry.find("maps.unique.ip").counter().count();
        assertEquals(2.0, count);
    }
}