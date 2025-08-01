package com.example.mapsbridge.config.metrics.tracker;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.ZoneOffset.UTC;

/**
 * Component for tracking unique IP addresses using the system daily.
 */
@Component
public class IpAddressTracker {

    private final MeterRegistry meterRegistry;
    // Store IPs seen today to avoid double counting
    private final Set<String> dailyUniqueIps = ConcurrentHashMap.newKeySet();
    private LocalDate currentDate = LocalDate.now(UTC);

    /**
     * Constructor with dependency injection.
     *
     * @param meterRegistry The meter registry
     */
    @Autowired
    public IpAddressTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Track a client IP address. If this IP hasn't been seen today,
     * it will increment the unique IP counter.
     *
     * @param ipAddress The client IP address
     */
    public void trackIpAddress(String ipAddress) {
        // Skip tracking if IP is null
        if (ipAddress == null) {
            return;
        }

        // Check if we need to reset for a new day
        LocalDate today = LocalDate.now(UTC);
        if (!today.equals(currentDate)) {
            resetDailyTracking(today);
        }

        // If this IP hasn't been seen today, count it
        if (dailyUniqueIps.add(ipAddress)) {
            incrementCounter();
        }
    }

    /**
     * Increments the unique IP counter.
     * This method is separated to make testing easier.
     */
    public void incrementCounter() {
        try {
            meterRegistry.counter("maps.unique.ip", "description", "Number of unique IP addresses using the system daily").increment();
        } catch (Exception e) {
            // Safely handle any issues with the counter
        }
    }

    /**
     * Reset the daily tracking when a new day starts.
     *
     * @param newDate The new date
     */
    private synchronized void resetDailyTracking(LocalDate newDate) {
        // Only reset if we're still behind (thread safety)
        if (newDate.isAfter(currentDate)) {
            dailyUniqueIps.clear();
            currentDate = newDate;
        }
    }

    /**
     * Get the current count of unique IPs for today.
     *
     * @return The number of unique IPs seen today
     */
    public int getUniqueIpCountForToday() {
        return dailyUniqueIps.size();
    }
}