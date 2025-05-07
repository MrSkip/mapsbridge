package com.example.mapsbridge.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.example.mapsbridge.exception.CoordinateExtractionException;
import com.example.mapsbridge.exception.InvalidCoordinateException;
import com.example.mapsbridge.exception.InvalidInputException;
import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.ConvertRequest;
import com.example.mapsbridge.model.ConvertResponse;
import com.example.mapsbridge.provider.MapProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for converting map URLs and coordinates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MapConverterService {

    private final List<MapProvider> mapProviders;

    // Pattern to match coordinates in the format "lat,lon"
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("^-?\\d+\\.?\\d*,-?\\d+\\.?\\d*$");

    // Pattern to match URLs
    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.*");

    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     * 
     * @param request The conversion request
     * @return The conversion response with coordinates and links
     * @throws InvalidCoordinateException if the coordinates are invalid
     * @throws CoordinateExtractionException if coordinates can't be extracted from a URL
     * @throws InvalidInputException if the input is neither coordinates nor a valid URL
     */
    public ConvertResponse convert(ConvertRequest request) {
        String input = request.getInput().trim();
        Coordinate coordinate;

        if (COORDINATE_PATTERN.matcher(input).matches()) {
            // Input is coordinates
            coordinate = Coordinate.fromString(input);
            if (!coordinate.isValid()) {
                throw new InvalidCoordinateException("Invalid coordinates: " + input);
            }
        } else if (URL_PATTERN.matcher(input).matches()) {
            // Input is a URL
            coordinate = extractCoordinatesFromUrl(input);
            if (coordinate == null) {
                throw new CoordinateExtractionException("Could not extract coordinates from URL: " + input);
            }
        } else {
            throw new InvalidInputException("Input must be coordinates (lat,lon) or a valid URL");
        }

        // Generate links for all providers
        ConvertResponse response = new ConvertResponse();
        response.setCoordinates(coordinate);

        for (MapProvider provider : mapProviders) {
            try {
                String url = provider.generateUrl(coordinate);
                response.addLink(provider.getType(), url);
            } catch (Exception e) {
                log.error("Error generating URL for provider {}: {}", provider.getType().getName(), e.getMessage());
            }
        }

        return response;
    }

    /**
     * Extract coordinates from a URL using the appropriate map provider.
     * 
     * @param url The URL to extract coordinates from
     * @return The extracted coordinates, or null if coordinates couldn't be extracted
     */
    private Coordinate extractCoordinatesFromUrl(String url) {
        // Try each provider to see if it can extract coordinates
        for (MapProvider provider : mapProviders) {
            if (provider.isProviderUrl(url)) {
                Coordinate coordinate = provider.extractCoordinates(url);
                if (coordinate != null && coordinate.isValid()) {
                    return coordinate;
                }
            }
        }

        // If no provider could extract coordinates, try all providers as a fallback
        for (MapProvider provider : mapProviders) {
            Coordinate coordinate = provider.extractCoordinates(url);
            if (coordinate != null && coordinate.isValid()) {
                return coordinate;
            }
        }

        return null;
    }
}
