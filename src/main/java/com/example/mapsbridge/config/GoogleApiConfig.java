package com.example.mapsbridge.config;

import com.google.maps.GeoApiContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Google API services.
 */
@Configuration
public class GoogleApiConfig {

    @Value("${google.api.key:}")
    private String googleApiKey;

    @Value("${google.api.enabled:true}")
    private boolean googleApiEnabled;

    /**
     * Creates a GeoApiContext bean for Google Maps API interactions.
     * If Google API is enabled but no API key is provided, throws an IllegalStateException.
     *
     * @return Configured GeoApiContext
     * @throws IllegalStateException if API is enabled but no key is provided
     */
    @Bean
    public GeoApiContext geoApiContext() {
        if (googleApiEnabled) {
            if (googleApiKey == null || StringUtils.isBlank(googleApiKey)) {
                throw new IllegalStateException("Google API is enabled but no API key is provided. " +
                        "Please set 'google.api.key' in application.properties or disable the API with 'google.api.enabled=false'");
            }
            
            return new GeoApiContext.Builder()
                    .apiKey(googleApiKey)
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(2, TimeUnit.SECONDS)
                    .writeTimeout(2, TimeUnit.SECONDS)
                    .maxRetries(2)
                    .build();
        }
        
        // Return a dummy context when API is disabled
        // This allows the bean to always be created, but it won't be used when API is disabled
        return new GeoApiContext.Builder().build();
    }
}