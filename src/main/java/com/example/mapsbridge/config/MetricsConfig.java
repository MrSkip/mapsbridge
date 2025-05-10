package com.example.mapsbridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Configuration for custom metrics using Micrometer.
 */
@Configuration
public class MetricsConfig {

    /**
     * Creates a counter builder for tracking map provider usage.
     * This will be used to count how many times each provider is requested by users.
     *
     * @param registry the meter registry
     * @return a counter builder for provider usage
     */
    @Bean
    public Counter.Builder providerUsageCounterBuilder(MeterRegistry registry) {
        return Counter.builder("maps.provider.usage")
                .description("Number of times each map provider is requested");
    }

    /**
     * Creates a counter builder for tracking which Google Maps extractor is used.
     * This will count how many times each extractor is used to fetch coordinates from Google Maps URLs.
     *
     * @param registry the meter registry
     * @return a counter builder for Google Maps extractors
     */
    @Bean
    public Counter.Builder googleMapsExtractorCounterBuilder(MeterRegistry registry) {
        return Counter.builder("maps.google.extractor.usage")
                .description("Number of times each extractor is used for Google Maps");
    }
}
