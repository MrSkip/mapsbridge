package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.GoogleCoordinateExtractor;
import com.example.mapsbridge.provider.extractor.impl.url.UrlPatternExtractor;
import com.example.mapsbridge.service.geocoding.HybridGeocodingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Final fallback extractor that geocodes address queries from Google Maps URLs.
 * This should be the last extractor in the chain, used when no coordinates or place IDs are found.
 * Uses the HybridGeocodingService which combines Radar.io and Google Maps APIs.
 */
@Component
@Order(7)
@Slf4j
public class G7AddressGeocodingExtractor implements GoogleCoordinateExtractor {

    private final HybridGeocodingService geocodingService;
    private final UrlPatternExtractor urlPatternExtractor;
    private final Counter addressGeocodingCounter;

    @Autowired
    public G7AddressGeocodingExtractor(
            HybridGeocodingService geocodingService,
            UrlPatternExtractor urlPatternExtractor,
            Counter.Builder geocodingExtractorSuccessCounterBuilder,
            MeterRegistry meterRegistry) {
        this.geocodingService = geocodingService;
        this.urlPatternExtractor = urlPatternExtractor;

        // Initialize counter for address geocoding success
        this.addressGeocodingCounter = geocodingExtractorSuccessCounterBuilder
                .tag("method", "addressGeocoding")
                .register(meterRegistry);
    }

    @Override
    public @NotNull LocationResult extract(String url) {
        if (url == null || url.trim().isEmpty()) {
            return new LocationResult();
        }

        try {
            log.debug("Processing URL for address geocoding: {}", url);

            return urlPatternExtractor.findAddressQuery(url)
                    .map(this::geocodeAddress)
                    .orElse(new LocationResult());
        } catch (Exception e) {
            log.error("Error geocoding address from URL: {}", e.getMessage());
            return new LocationResult();
        }
    }

    /**
     * Geocodes an address query to location information using the geocoding service.
     *
     * @param query The address query to geocode
     * @return LocationResult with coordinates if successful, empty LocationResult if not
     */
    private LocationResult geocodeAddress(String query) {
        log.debug("Found address query: {}, attempting to geocode", query);
        LocationResult result = geocodingService.geocodeQuery(query);

        if (result != null && result.hasValidCoordinates()) {
            // Set the original query as the address in the result
            result.setAddress(query);

            log.debug("Successfully geocoded address to coordinates: {},{} - Address: {}",
                    result.getCoordinates().getLat(),
                    result.getCoordinates().getLon(),
                    result.getAddress());

            addressGeocodingCounter.increment();
            return result;
        }

        return new LocationResult();
    }
}