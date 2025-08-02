package com.example.mapsbridge.service;

import com.example.mapsbridge.config.metrics.MetricTags;
import com.example.mapsbridge.config.metrics.tracker.InputSourceTracker;
import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.exception.CoordinateExtractionException;
import com.example.mapsbridge.exception.InvalidCoordinateException;
import com.example.mapsbridge.exception.InvalidInputException;
import com.example.mapsbridge.provider.MapProvider;
import com.example.mapsbridge.service.geocoding.HybridGeocodingService;
import com.example.mapsbridge.util.InputPatterns;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;

import static com.example.mapsbridge.util.InputPatterns.URL_EXTRACTION_PATTERN;

/**
 * Service for processing input and extracting coordinates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInputProcessorService {
    private final List<MapProvider> mapProviders;
    private final InputSourceTracker inputSourceTracker;
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
        return processInput(input, false);
    }

    public LocationResult processInput(String input, boolean skipReverseGeocode) {
        // Extract URL if input contains a URL embedded in text
        input = extractUrlFromText(input);

        if (isCoordinateInput(input)) {
            return processCoordinateInput(input, skipReverseGeocode);
        } else if (isUrlInput(input)) {
            return processUrlInput(input, skipReverseGeocode);
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

    private LocationResult processCoordinateInput(String input, boolean skipReverseGeocode) {
        inputSourceTracker.trackInputType(MetricTags.COORDINATES);

        Coordinate coordinate = Coordinate.fromString(input.trim());
        if (!coordinate.isValid()) {
            throw new InvalidCoordinateException("Invalid coordinates: " + input);
        }

        if (skipReverseGeocode) {
            return LocationResult.fromCoordinates(coordinate);
        } else {
            return geocodingService.reverseGeocode(coordinate);
        }
    }

    private LocationResult processUrlInput(String input, boolean skipReverseGeocode) {
        inputSourceTracker.trackInputType(MetricTags.URL);

        LocationResult locationResult = extractLocationFromUrl(input);
        if (locationResult == null || !locationResult.hasValidCoordinates()) {
            throw new CoordinateExtractionException("Could not extract location information from URL: " + input);
        }

        if (StringUtils.isBlank(locationResult.getAddress()) && !skipReverseGeocode) {
            LocationResult reverseGeocode = geocodingService.reverseGeocode(locationResult.getCoordinates());
            reverseGeocode.setMapSource(locationResult.getMapSource());
            reverseGeocode.setOriginalUrl(input);
            return reverseGeocode;
        }

        locationResult.setOriginalUrl(input);
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

        inputSourceTracker.trackMapProviderUrl(MetricTags.UNKNOWN);
        log.info("Could not extract location information from URL: {}", url);
        return null;
    }

    private LocationResult processProviderUrlForLocation(MapProvider provider, String url) {
        inputSourceTracker.trackMapProviderUrl(provider.getType().getName());

        LocationResult locationResult = provider.extractLocation(url);
        return (locationResult != null && locationResult.hasValidCoordinates()) ? locationResult : null;
    }

    /**
     * Extracts a URL from a text string that may contain other content.
     *
     * @param text The text that may contain a URL
     * @return The extracted URL or the original text if no URL is found
     */
    private String extractUrlFromText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Use a regex to find a URL in the text
        Matcher matcher = URL_EXTRACTION_PATTERN.matcher(text);
        if (matcher.find()) {
            // Return the found URL
            return matcher.group();
        }

        // Return original text if no URL is found
        return text;
    }
}
