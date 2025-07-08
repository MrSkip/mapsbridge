package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.GoogleCoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that parses coordinates from q=LAT,LON parameters in Google Maps URLs.
 *
 * <p>This extractor supports both standard decimal format (40.7128,-74.0060)
 * and European format with comma as decimal separator (40,7128,-74,0060).</p>
 */
@Component
@Order(4)
@Slf4j
public class G4QParameterExtractor implements GoogleCoordinateExtractor {

    /**
     * Pattern to match coordinates in q parameter.
     * Supports both dot and comma as decimal separators.
     * Named groups: lat, lon
     */
    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            "q=(?<lat>-?\\d+[.,]?\\d*),(?<lon>-?\\d+[.,]?\\d*)"
    );

    @Override
    public @NotNull LocationResult extract(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        String decodedUrl = decodeUrl(url);
        return extractCoordinatesFromUrl(decodedUrl);
    }

    /**
     * Decodes the URL to handle encoded characters.
     *
     * @param url The URL to decode
     * @return The decoded URL, or original URL if decoding fails
     */
    private String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Error decoding URL: {}", url, e);
            return url;
        }
    }

    /**
     * Extracts coordinates from the decoded URL.
     *
     * @param decodedUrl The decoded URL to process
     * @return LocationResult with coordinates if found, empty result otherwise
     */
    private LocationResult extractCoordinatesFromUrl(String decodedUrl) {
        Matcher matcher = COORDINATE_PATTERN.matcher(decodedUrl);

        if (!matcher.find()) {
            return new LocationResult();
        }

        try {
            Coordinate coordinate = parseCoordinates(matcher.group("lat"), matcher.group("lon"));
            log.debug("Extracted coordinates from q parameter: {},{}", coordinate.getLat(), coordinate.getLon());
            return LocationResult.fromCoordinates(coordinate);
        } catch (NumberFormatException e) {
            log.warn("Invalid coordinate format in q parameter: {}", decodedUrl);
            return new LocationResult();
        }
    }

    /**
     * Parses coordinate strings and normalizes European decimal comma format.
     *
     * @param latStr The latitude string
     * @param lonStr The longitude string
     * @return Coordinate object with parsed values
     * @throws NumberFormatException if parsing fails
     */
    private Coordinate parseCoordinates(String latStr, String lonStr) {
        // Normalize European decimal commas to dots
        String normalizedLat = latStr.replace(',', '.');
        String normalizedLon = lonStr.replace(',', '.');

        double lat = Double.parseDouble(normalizedLat);
        double lon = Double.parseDouble(normalizedLon);

        return new Coordinate(lat, lon);
    }
}