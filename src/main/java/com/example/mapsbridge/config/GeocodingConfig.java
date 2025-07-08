package com.example.mapsbridge.config;

import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for geocoding services.
 */
@Configuration
@EnableCaching
public class GeocodingConfig {

    /**
     * Provides Radar API key property placeholder.
     */
    @Value("${radar.api.key:}")
    private String radarApiKey;
    /**
     * Provides Radar API enabled property placeholder.
     */
    @Value("${radar.api.enabled:false}")
    private boolean radarApiEnabled;

    /**
     * Creates a Counter.Builder for geocoding services.
     *
     * @return The Counter.Builder
     */
    @Bean
    public Counter.Builder geocodingCounterBuilder() {
        return Counter.builder("geocoding.requests")
                .description("Number of geocoding requests");
    }
}
