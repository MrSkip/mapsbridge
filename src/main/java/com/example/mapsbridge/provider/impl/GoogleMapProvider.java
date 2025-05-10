package com.example.mapsbridge.provider.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.mapsbridge.exception.CoordinateExtractionException;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.service.GoogleGeocodingService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
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
        return getCoordinatesFromGeocodingApi(url);
    }

    protected Coordinate getCoordinatesFromGeocodingApi(String url) {
        if (!geocodingService.isApiEnabled()) {
            return null;
        }
        try {
            String placeId = findPlaceId(url);
            if (placeId != null) {
                // Get place details from Google API service
                return geocodingService.getPlaceCoordinates(placeId);
            }

            // Extract query from URL
            String address = findAddress(url);

            if (address != null) {
                return geocodingService.geocodeQuery(address);
            }
        } catch (Exception e) {
            log.error("Error getting coordinates from Google API", e);
        }

        return null;
    }

    protected String findAddress(String url) {
        Pattern queryPattern = Pattern.compile("q=([^&]+)");
        Matcher queryMatcher = queryPattern.matcher(url);
        if (queryMatcher.find()) {
            String query = queryMatcher.group(1);
            query = query.replace("+", " ");
            log.debug("Extracted query: {}", query);
            return query;
        }
        return null;
    }

    protected String findPlaceId(String url) {
        // First, check for place_id= parameter
        Pattern placeIdPattern = Pattern.compile("place_id=([\\w\\-]+)");
        Matcher placeIdMatcher = placeIdPattern.matcher(url);
        if (placeIdMatcher.find()) {
            String placeId = placeIdMatcher.group(1);
            log.debug("Extracted place_id: {}", placeId);
            return placeId;
        }

        // Then, check for !1s pattern (common in Google Maps URLs)
        Pattern placeIdPattern2 = Pattern.compile("!1s([\\w\\-:]+)");
        Matcher placeIdMatcher2 = placeIdPattern2.matcher(url);
        if (placeIdMatcher2.find()) {
            String placeId = placeIdMatcher2.group(1);
            log.debug("Extracted place_id from !1s pattern: {}", placeId);
            return placeId;
        }

        // Finally, check for !3m5!1s pattern (another common format)
        Pattern placeIdPattern3 = Pattern.compile("!3m\\d+!1s([\\w\\-:]+)");
        Matcher placeIdMatcher3 = placeIdPattern3.matcher(url);
        if (placeIdMatcher3.find()) {
            String placeId = placeIdMatcher3.group(1);
            log.debug("Extracted place_id from !3m!1s pattern: {}", placeId);
            return placeId;
        }

        return null;
    }
}
