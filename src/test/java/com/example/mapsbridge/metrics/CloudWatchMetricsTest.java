package com.example.mapsbridge.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test to verify CloudWatch metrics integration.
 * This test creates a test metric and verifies that it can be registered with the MeterRegistry.
 * If the test passes, it indicates that the metrics system is properly configured.
 * <p>
 * Note: This test doesn't verify that metrics actually appear in CloudWatch,
 * as that would require an actual AWS connection. It only verifies that the
 * metrics are properly collected and ready to be sent.
 */
@SpringBootTest
@ActiveProfiles("test")
public class CloudWatchMetricsTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    public void testMetricsRegistration() {
        // Create a test counter
        Counter counter = Counter.builder("test.metric")
                .description("A test metric to verify CloudWatch integration")
                .tag("test", "true")
                .register(meterRegistry);

        // Increment the counter
        counter.increment();

        // Verify the counter was registered
        assertNotNull(meterRegistry.find("test.metric").counter());

        System.out.println("[DEBUG_LOG] Test metric registered: " +
                meterRegistry.find("test.metric").counter().count());
    }
}