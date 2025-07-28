package com.example.mapsbridge.provider.extractor.google;

import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.url.UrlPatternExtractor;
import com.example.mapsbridge.service.geocoding.HybridGeocodingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Extractor that extracts place IDs from Google Maps URLs and resolves them to coordinates.
 * This is part of the Chain of Responsibility pattern for coordinate extraction.
 *
 * <p>This extractor handles Google Maps URLs containing place IDs and uses the geocoding
 * service to resolve them to actual coordinates and location information.</p>
 *
 * <p><strong>Processing Order:</strong> Executed with order 6 in the extraction chain,
 * after other coordinate extraction methods have been attempted.</p>
 */
@Component
@Order(6)
@Slf4j
public class G6PlaceIdExtractor implements GoogleCoordinateExtractor {

    private final HybridGeocodingService geocodingService;
    private final UrlPatternExtractor urlPatternExtractor;

    @Autowired
    public G6PlaceIdExtractor(
            HybridGeocodingService geocodingService,
            UrlPatternExtractor urlPatternExtractor) {
        this.geocodingService = geocodingService;
        this.urlPatternExtractor = urlPatternExtractor;
    }

    @Override
    public @NotNull LocationResult extract(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        try {
            log.debug("Processing URL for place ID extraction: {}", url);
            return extractPlaceIdAndResolve(url);
        } catch (Exception e) {
            log.error("Error extracting place ID from URL: {}", e.getMessage());
            return new LocationResult();
        }
    }

    /**
     * Extracts place ID from URL and resolves it to location information.
     *
     * @param url The URL to process
     * @return LocationResult with coordinates if successful, empty LocationResult otherwise
     */
    private LocationResult extractPlaceIdAndResolve(String url) {
        return urlPatternExtractor.findPlaceId(url)
                .map(this::resolveLocationFromPlaceId)
                .orElse(new LocationResult());
    }

    /**
     * Resolves a place ID to location information using the geocoding service.
     *
     * @param placeId The place ID to resolve
     * @return LocationResult with coordinates if successful, empty LocationResult if not
     */
    private LocationResult resolveLocationFromPlaceId(String placeId) {
        log.debug("Found place ID: {}, attempting to resolve", placeId);

        LocationResult result = geocodingService.getLocationFromPlaceId(placeId);

        if (isValidLocationResult(result)) {
            log.debug("Successfully resolved place ID {}", result);
            return result;
        }

        return new LocationResult();
    }

    /**
     * Checks if the location result is valid and has coordinates.
     *
     * @param result The location result to validate
     * @return true if result is valid and has coordinates, false otherwise
     */
    private boolean isValidLocationResult(LocationResult result) {
        return result != null && result.hasValidCoordinates();
    }
}