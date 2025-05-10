package com.example.mapsbridge.provider.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.mapsbridge.exception.CoordinateExtractionException;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.service.GoogleGeocodingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.OkHttpClient;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;

/**
 * Google Maps provider implementation.
 */
@Service
@Slf4j
public class GoogleMapProvider extends AbstractMapProvider {

    private final GoogleGeocodingService geocodingService;

    /**
     * Constructor with dependency injection.
     * 
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     * @param geocodingService The Google Geocoding service
     */
    public GoogleMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.google.url:https://www.google.com/maps?q={lat},{lon}}") String urlTemplate,
            GoogleGeocodingService geocodingService) {
        super(httpClient, urlTemplate);

        // Initialize URL patterns
        this.urlPattern = Pattern.compile("https?://(www\\.)?google\\.com/maps.*|https?://maps\\.app\\.goo\\.gl/.*|https?://goo\\.gl/maps/.*");

        // Pattern for q=lat,lon format
        this.coordinatePattern = Pattern.compile("q=(?<lat>-?\\d+\\.?\\d*),(?<lon>-?\\d+\\.?\\d*)");

        this.geocodingService = geocodingService;
    }

    @Override
    public MapType getType() {
        return MapType.GOOGLE;
    }

    @Override
    public Coordinate extractCoordinates(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        // Step 1: Check if URL is a shortened redirect
        if (url.startsWith("https://maps.app.goo.gl/") || url.startsWith("https://goo.gl/maps/")) {
            log.debug("Resolving shortened URL: {}", url);
            String resolvedUrl = followRedirects(url);
            if (!url.equals(resolvedUrl)) {
                log.debug("Resolved to: {}", resolvedUrl);
                url = resolvedUrl;
            }
        }

        // Step 2: Check for !3dLAT!4dLON pattern
        Pattern pattern3d4d = Pattern.compile("!3d([\\-\\d.]+)!4d([\\-\\d.]+)");
        Matcher matcher3d4d = pattern3d4d.matcher(url);

        double lastLat = 0.0;
        double lastLon = 0.0;
        boolean found = false;

        while (matcher3d4d.find()) {
            try {
                lastLat = Double.parseDouble(matcher3d4d.group(1));
                lastLon = Double.parseDouble(matcher3d4d.group(2));
                found = true;
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinate format in !3d!4d pattern: {}", url);
            }
        }

        if (found) {
            log.debug("Extracted coordinates from last !3d!4d pattern: {},{}", lastLat, lastLon);
            return new Coordinate(lastLat, lastLon);
        }

        // Step 3: Check for @LAT,LON pattern
        Pattern patternAt = Pattern.compile("@([\\-\\d.]+),([\\-\\d.]+)");
        Matcher matcherAt = patternAt.matcher(url);
        if (matcherAt.find()) {
            try {
                double lat = Double.parseDouble(matcherAt.group(1));
                double lon = Double.parseDouble(matcherAt.group(2));
                log.debug("Extracted coordinates from @ pattern: {},{}", lat, lon);
                return new Coordinate(lat, lon);
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinate format in @ pattern: {}", url);
            }
        }

        // Step 5 Try the default coordinate pattern (q=lat,lon)
        Coordinate coordinate = super.extractCoordinates(url);
        if (coordinate != null) {
            return coordinate;
        }

        // Step 6
        Pattern searchPattern = Pattern.compile("/search/([-+]?\\d+\\.\\d+),([-+]?\\d+\\.\\d+)");
        Matcher searchMatcher = searchPattern.matcher(url);

        if (searchMatcher.find()) {
            try {
                double lat = Double.parseDouble(searchMatcher.group(1));
                double lon = Double.parseDouble(searchMatcher.group(2));
                log.debug("Extracted coordinates from /search/ pattern: {},{}", lat, lon);
                return new Coordinate(lat, lon);
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinate format in /search/ pattern: {}", url);
            }
        }

        // Step 6: Check for q= (search query)
        if (geocodingService.isApiEnabled()) {
            try {
                // Extract query from URL
                Pattern queryPattern = Pattern.compile("q=([^&]+)");
                Matcher queryMatcher = queryPattern.matcher(url);
                if (queryMatcher.find()) {
                    String query = queryMatcher.group(1);
                    query = query.replace("+", " ");
                    log.debug("Extracted query: {}", query);

                    // Call Google Geocoding service
                    Coordinate geocodedCoordinate = geocodingService.geocodeQuery(query);
                    if (geocodedCoordinate != null) {
                        return geocodedCoordinate;
                    }
                }

                // Try place_id if query not found
                Pattern placeIdPattern = Pattern.compile("place_id=([\\w\\-]+)");
                Matcher placeIdMatcher = placeIdPattern.matcher(url);
                if (placeIdMatcher.find()) {
                    String placeId = placeIdMatcher.group(1);
                    log.debug("Extracted place_id: {}", placeId);

                    // Get place details from Google API service
                    Coordinate placeCoordinate = geocodingService.getPlaceCoordinates(placeId);
                    if (placeCoordinate != null) {
                        return placeCoordinate;
                    }
                }
            } catch (Exception e) {
                log.error("Error getting coordinates from Google API", e);
            }
        }

        return null;
    }
}
