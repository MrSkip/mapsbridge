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

    /**
     * Creates a counter builder for tracking successful matches in GeocodingApiFallbackExtractor.
     * This will count how many times each method or pattern successfully finds what it's looking for.
     *
     * @param registry the meter registry
     * @return a counter builder for GeocodingApiFallbackExtractor success rates
     */
    @Bean
    public Counter.Builder geocodingExtractorSuccessCounterBuilder(MeterRegistry registry) {
        return Counter.builder("maps.geocoding.extractor.success")
                .description("Number of successful matches in GeocodingApiFallbackExtractor");
    }

    /**
     * Creates a counter for tracking input types (coordinates vs URL).
     * This will count how many times users request conversion using plain coordinates vs URLs.
     *
     * @param registry the meter registry
     * @return a counter builder for input type tracking
     */
    @Bean
    public Counter.Builder inputTypeCounterBuilder(MeterRegistry registry) {
        return Counter.builder("maps.input.type")
                .description("Number of times each input type is used (coordinates vs URL)");
    }

    /**
     * Creates a counter for tracking which map provider URLs are used as input.
     * This will count how many times URLs from each provider are submitted for conversion.
     *
     * @param registry the meter registry
     * @return a counter builder for map provider URL tracking
     */
    @Bean
    public Counter.Builder mapProviderUrlCounterBuilder(MeterRegistry registry) {
        return Counter.builder("maps.provider.url.usage")
                .description("Number of times URLs from each map provider are used as input");
    }
}
