package com.example.mapsbridge.config.metrics.tracker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component for tracking metrics related to geocoding operations.
 */
@Component
public class GeocodingTracker {

    private final MeterRegistry meterRegistry;
    private final Counter.Builder geocodingCounterBuilder;

    /**
     * Constructor with dependency injection.
     *
     * @param meterRegistry The meter registry
     */
    @Autowired
    public GeocodingTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Create counter builder for tracking geocoding operations
        this.geocodingCounterBuilder = Counter.builder("geocoding.operation")
                .description("Number of geocoding operations by service and operation type");
    }

    /**
     * Track a reverse geocoding operation.
     *
     * @param service The geocoding service name
     */
    public void trackReverseGeocode(String service) {
        geocodingCounterBuilder
                .tag("service", service)
                .tag("operation", "reverseGeocode")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Track a forward geocoding operation.
     *
     * @param service The geocoding service name
     */
    public void trackForwardGeocode(String service) {
        geocodingCounterBuilder
                .tag("service", service)
                .tag("operation", "forwardGeocode")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Track a place ID lookup operation.
     *
     * @param service The geocoding service name
     */
    public void trackPlaceIdLookup(String service) {
        geocodingCounterBuilder
                .tag("service", service)
                .tag("operation", "placeIdLookup")
                .register(meterRegistry)
                .increment();
    }
}