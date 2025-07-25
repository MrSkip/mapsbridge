package com.example.mapsbridge.service;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.exception.CoordinateExtractionException;
import com.example.mapsbridge.exception.InvalidCoordinateException;
import com.example.mapsbridge.exception.InvalidInputException;
import com.example.mapsbridge.metrics.MetricTags;
import com.example.mapsbridge.provider.MapProvider;
import com.example.mapsbridge.service.geocoding.HybridGeocodingService;
import com.example.mapsbridge.util.InputPatterns;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for processing input and extracting coordinates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInputProcessorService {
    private final List<MapProvider> mapProviders;
    private final Counter.Builder inputTypeCounterBuilder;
    private final Counter.Builder mapProviderUrlCounterBuilder;
    private final MeterRegistry meterRegistry;
    private final HybridGeocodingService geocodingService;

    /**
     * Process input and extract location information.
     *
     * @param input The input string (coordinates or URL)
     * @return The extracted location result containing coordinates and location name
     * @throws InvalidInputException         if the input format is invalid
     * @throws InvalidCoordinateException    if the coordinates are invalid
     * @throws CoordinateExtractionException if location information cannot be extracted from the URL
     */
    public LocationResult processInput(String input) {
        if (isCoordinateInput(input)) {
            return processCoordinateInput(input);
        } else if (isUrlInput(input)) {
            return processUrlInput(input);
        } else {
            throw new InvalidInputException("Input must be coordinates (lat,lon) or a valid URL");
        }
    }

    private boolean isCoordinateInput(String input) {
        return InputPatterns.COORDINATE_PATTERN.matcher(input).matches();
    }

    private boolean isUrlInput(String input) {
        return InputPatterns.URL_PATTERN.matcher(input).matches();
    }

    private LocationResult processCoordinateInput(String input) {
        trackMetric(inputTypeCounterBuilder, MetricTags.INPUT_TYPE, MetricTags.COORDINATES);

        Coordinate coordinate = Coordinate.fromString(input.trim());
        if (!coordinate.isValid()) {
            throw new InvalidCoordinateException("Invalid coordinates: " + input);
        }

        return geocodingService.reverseGeocode(coordinate);
    }

    private LocationResult processUrlInput(String input) {
        trackMetric(inputTypeCounterBuilder, MetricTags.INPUT_TYPE, MetricTags.URL);

        LocationResult locationResult = extractLocationFromUrl(input);
        if (locationResult == null || !locationResult.hasValidCoordinates()) {
            throw new CoordinateExtractionException("Could not extract location information from URL: " + input);
        }

        if (StringUtils.isBlank(locationResult.getAddress())) {
            LocationResult reverseGeocode = geocodingService.reverseGeocode(locationResult.getCoordinates());
            reverseGeocode.setMapSource(locationResult.getMapSource());
            return reverseGeocode;
        }
        return locationResult;
    }

    /**
     * Extract location information from a URL using the appropriate map provider.
     */
    private LocationResult extractLocationFromUrl(String url) {
        for (MapProvider provider : mapProviders) {
            if (provider.isProviderUrl(url)) {
                return processProviderUrlForLocation(provider, url);
            }
        }

        trackMetric(mapProviderUrlCounterBuilder, MetricTags.PROVIDER, MetricTags.UNKNOWN);
        log.info("Could not extract location information from URL: {}", url);
        return null;
    }

    private LocationResult processProviderUrlForLocation(MapProvider provider, String url) {
        trackMetric(mapProviderUrlCounterBuilder, MetricTags.PROVIDER, provider.getType().getName());

        LocationResult locationResult = provider.extractLocation(url);
        return (locationResult != null && locationResult.hasValidCoordinates()) ? locationResult : null;
    }

    private void trackMetric(Counter.Builder counterBuilder, String tagKey, String tagValue) {
        counterBuilder
                .tag(tagKey, tagValue)
                .register(meterRegistry)
                .increment();
    }
}
