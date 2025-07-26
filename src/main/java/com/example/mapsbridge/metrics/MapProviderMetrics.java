package com.example.mapsbridge.metrics;

import com.example.mapsbridge.dto.MapType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component for tracking metrics related to map providers and extractors.
 */
@Component
public class MapProviderMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter.Builder extractionSuccessCounterBuilder;
    private final Counter.Builder extractionFailureCounterBuilder;

    /**
     * Constructor with dependency injection.
     *
     * @param meterRegistry The meter registry
     */
    @Autowired
    public MapProviderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Create counter builders for tracking extraction success and failure
        this.extractionSuccessCounterBuilder = Counter.builder("maps.extraction.success")
                .description("Number of successful extractions by map provider and extractor");

        this.extractionFailureCounterBuilder = Counter.builder("maps.extraction.failure")
                .description("Number of failed extractions by map provider and extractor");
    }

    /**
     * Track a successful extraction.
     *
     * @param mapProvider   The map provider type
     * @param extractorName The name of the extractor used
     */
    public void trackExtractionSuccess(MapType mapProvider, String extractorName) {
        extractionSuccessCounterBuilder
                .tag(MetricTags.PROVIDER, mapProvider.getName())
                .tag("extractor", extractorName)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Track a failed extraction.
     *
     * @param mapProvider   The map provider type
     * @param extractorName The name of the extractor used
     */
    public void trackExtractionFailure(MapType mapProvider, String extractorName) {
        extractionFailureCounterBuilder
                .tag(MetricTags.PROVIDER, mapProvider.getName())
                .tag("extractor", extractorName)
                .register(meterRegistry)
                .increment();
    }
}