package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.GoogleCoordinateExtractor;
import com.example.mapsbridge.provider.extractor.impl.url.UrlPatternExtractor;
import com.example.mapsbridge.service.geocoding.HybridGeocodingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Fallback extractor that performs address geocoding when no coordinates found.
 * This should be the last extractor in the chain.
 * Uses the HybridGeocodingService which combines Radar.io and Google Maps APIs.
 */
@Component
@Order(6)
@Slf4j
public class G6GeocodingApiFallbackExtractor implements GoogleCoordinateExtractor {

    private final HybridGeocodingService geocodingService;
    private final UrlPatternExtractor urlPatternExtractor;
    private final Counter geocodingSuccessCounter;
    private final Counter urlProcessingCounter;

    @Autowired
    public G6GeocodingApiFallbackExtractor(
            HybridGeocodingService geocodingService,
            UrlPatternExtractor urlPatternExtractor,
            Counter.Builder geocodingExtractorSuccessCounterBuilder,
            @Qualifier("inputTypeCounterBuilder") Counter.Builder counterBuilder,
            MeterRegistry meterRegistry) {
        this.geocodingService = geocodingService;
        this.urlPatternExtractor = urlPatternExtractor;

        // Initialize counter for geocoding success
        this.geocodingSuccessCounter = geocodingExtractorSuccessCounterBuilder
                .tag("method", "hybridGeocoding")
                .register(meterRegistry);

        // Initialize counter for URL processing
        this.urlProcessingCounter = counterBuilder
                .tag("service", "url-processor")
                .tag("type", "url")
                .register(meterRegistry);
    }

    @Override
//    @Cacheable("url-processing")
    public @NotNull LocationResult extract(String url) {
        if (url == null || url.trim().isEmpty()) {
            return new LocationResult();
        }

        try {
            // Process the URL to extract location information
            LocationResult result = processUrl(url);

            if (result != null && result.hasValidCoordinates()) {
                log.debug("Extracted coordinates: {},{} - Location: {}",
                        result.getCoordinates().getLat(),
                        result.getCoordinates().getLon(),
                        result.getAddress());

                geocodingSuccessCounter.increment();
                return result;
            }
        } catch (Exception e) {
            log.error("Error extracting coordinates from URL: {}", e.getMessage());
        }

        return new LocationResult();
    }

    /**
     * Processes a URL to extract location information.
     * This is a high-level method that analyzes the URL and determines the best approach.
     */
    public LocationResult processUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        urlProcessingCounter.increment();
        log.debug("Processing URL: {}", url);

        // Try to extract coordinates directly from the URL
        Optional<Coordinate> coordinates = urlPatternExtractor.findCoordinates(url);
        if (coordinates.isPresent()) {
            log.debug("Found coordinates in URL: {}", coordinates.get());
            return geocodingService.reverseGeocode(coordinates.get());
        }

        // Try to extract place ID
        return urlPatternExtractor.findPlaceId(url)
                .flatMap(this::tryGetLocationFromPlaceId)
                // If place ID extraction fails, try to geocode the address
                .orElseGet(() -> urlPatternExtractor.findAddressQuery(url)
                        .flatMap(this::tryGeocodeAddress)
                        .orElse(new LocationResult()));
    }

    private Optional<LocationResult> tryGetLocationFromPlaceId(String placeId) {
        LocationResult result = geocodingService.getLocationFromPlaceId(placeId);
        return (result != null && result.hasValidCoordinates()) ? Optional.of(result) : Optional.empty();
    }

    private Optional<LocationResult> tryGeocodeAddress(String query) {
        LocationResult result = geocodingService.geocodeQuery(query);
        if (result != null && result.hasValidCoordinates()) {
            result.setAddress(query);
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
