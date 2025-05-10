package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.provider.extractor.CoordinateExtractor;
import com.example.mapsbridge.service.GoogleGeocodingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fallback extractor that performs address geocoding when no coordinates found.
 * This should be the last extractor in the chain.
 */
@Component
@Order(6) // Execute last as a fallback
@Slf4j
public class GeocodingApiFallbackExtractor implements CoordinateExtractor {

    private final GoogleGeocodingService geocodingService;
    private final Counter findAddressSuccessCounter;
    private final Counter placeIdMatcherSuccessCounter;
    private final Counter placeIdMatcher2SuccessCounter;
    private final Counter placeIdMatcher3SuccessCounter;

    // Patterns for finding place IDs and addresses
    private static final Pattern PLACE_ID_PATTERN = Pattern.compile("place_id=([\\w\\-]+)");
    private static final Pattern PLACE_ID_PATTERN_2 = Pattern.compile("!1s([\\w\\-:]+)");
    private static final Pattern PLACE_ID_PATTERN_3 = Pattern.compile("!3m\\d+!1s([\\w\\-:]+)");
    private static final Pattern QUERY_PATTERN = Pattern.compile("q=([^&]+)");

    @Autowired
    public GeocodingApiFallbackExtractor(
            GoogleGeocodingService geocodingService,
            Counter.Builder geocodingExtractorSuccessCounterBuilder,
            MeterRegistry meterRegistry) {
        this.geocodingService = geocodingService;

        // Initialize counters for each method/pattern
        this.findAddressSuccessCounter = geocodingExtractorSuccessCounterBuilder
                .tag("method", "findAddress")
                .register(meterRegistry);

        this.placeIdMatcherSuccessCounter = geocodingExtractorSuccessCounterBuilder
                .tag("method", "placeIdMatcher")
                .register(meterRegistry);

        this.placeIdMatcher2SuccessCounter = geocodingExtractorSuccessCounterBuilder
                .tag("method", "placeIdMatcher2")
                .register(meterRegistry);

        this.placeIdMatcher3SuccessCounter = geocodingExtractorSuccessCounterBuilder
                .tag("method", "placeIdMatcher3")
                .register(meterRegistry);
    }

    @Override
    public Coordinate extract(String url) {
        if (url == null || url.trim().isEmpty() || !geocodingService.isApiEnabled()) {
            return null;
        }

        try {
            // try to extract address query (it is less expensive than place_id)
            String address = findAddress(url);
            if (address != null) {
                Coordinate coordinate = geocodingService.geocodeQuery(address);
                if (coordinate != null) {
                    log.debug("Extracted coordinates from address query: {}", address);
                    return coordinate;
                }
            }

            // First try to extract place ID
            String placeId = findPlaceId(url);
            if (placeId != null) {
                Coordinate coordinate = geocodingService.getPlaceCoordinates(placeId);
                if (coordinate != null) {
                    log.debug("Extracted coordinates from place ID: {}", placeId);
                    return coordinate;
                }
            }
        } catch (Exception e) {
            log.error("Error getting coordinates from Google API", e);
        }

        return null;
    }

    /**
     * Find a place ID in the URL.
     * 
     * @param url The URL to search
     * @return The place ID if found, null otherwise
     */
    public String findPlaceId(String url) {
        // First, check for place_id= parameter
        Matcher placeIdMatcher = PLACE_ID_PATTERN.matcher(url);
        if (placeIdMatcher.find()) {
            String placeId = placeIdMatcher.group(1);
            log.debug("Extracted place_id: {}", placeId);
            placeIdMatcherSuccessCounter.increment();
            return placeId;
        }

        // Then, check for !1s pattern (common in Google Maps URLs)
        Matcher placeIdMatcher2 = PLACE_ID_PATTERN_2.matcher(url);
        if (placeIdMatcher2.find()) {
            String placeId = placeIdMatcher2.group(1);
            log.debug("Extracted place_id from !1s pattern: {}", placeId);
            placeIdMatcher2SuccessCounter.increment();
            return placeId;
        }

        // Finally, check for !3m5!1s pattern (another common format)
        Matcher placeIdMatcher3 = PLACE_ID_PATTERN_3.matcher(url);
        if (placeIdMatcher3.find()) {
            String placeId = placeIdMatcher3.group(1);
            log.debug("Extracted place_id from !3m!1s pattern: {}", placeId);
            placeIdMatcher3SuccessCounter.increment();
            return placeId;
        }

        return null;
    }

    /**
     * Find an address query in the URL.
     * 
     * @param url The URL to search
     * @return The address query if found, null otherwise
     */
    public String findAddress(String url) {
        Matcher queryMatcher = QUERY_PATTERN.matcher(url);
        if (queryMatcher.find()) {
            String query = queryMatcher.group(1);
            try {
                // Use URLDecoder instead of manual replacement
                query = URLDecoder.decode(query, StandardCharsets.UTF_8);
                log.debug("Extracted query: {}", query);
                findAddressSuccessCounter.increment();
                return query;
            } catch (Exception e) {
                log.warn("Error decoding query: {}", query, e);
                // Fallback to manual replacement if decoding fails
                query = query.replace("+", " ");
                findAddressSuccessCounter.increment();
                return query;
            }
        }
        return null;
    }
}
