package com.example.mapsbridge.config.metrics.tracker;

import com.example.mapsbridge.config.metrics.MetricTags;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component for tracking metrics related to user input processing.
 */
@Component
public class InputSourceTracker {

    private final MeterRegistry meterRegistry;
    private final Counter.Builder inputTypeCounterBuilder;
    private final Counter.Builder mapProviderUrlCounterBuilder;

    /**
     * Constructor with dependency injection.
     *
     * @param meterRegistry The meter registry
     */
    @Autowired
    public InputSourceTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Create counter builders for tracking input types and map provider URLs
        this.inputTypeCounterBuilder = Counter.builder("maps.input.type")
                .description("Number of times each input type is used (coordinates vs URL)");

        this.mapProviderUrlCounterBuilder = Counter.builder("maps.provider.url.usage")
                .description("Number of times URLs from each map provider are used as input");
    }

    /**
     * Track the type of input used (coordinates or URL).
     *
     * @param inputType The type of input (coordinates or URL)
     */
    public void trackInputType(String inputType) {
        inputTypeCounterBuilder
                .tag(MetricTags.INPUT_TYPE, inputType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Track which map provider URL was used as input.
     *
     * @param providerName The name of the map provider
     */
    public void trackMapProviderUrl(String providerName) {
        mapProviderUrlCounterBuilder
                .tag(MetricTags.PROVIDER, providerName)
                .register(meterRegistry)
                .increment();
    }
}