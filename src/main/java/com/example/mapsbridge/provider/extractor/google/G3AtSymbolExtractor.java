package com.example.mapsbridge.provider.extractor.google;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.mapsbridge.provider.utils.PlaceNameDecoder.extractPlaceName;

/**
 * Extractor that parses coordinates from @LAT,LON segments in Google Maps URLs.
 * Also extracts place names when present in /place/ URLs.
 *
 * <p>This extractor handles Google Maps URLs that contain coordinates in the format {@code @latitude,longitude}
 * and can optionally extract place names from URLs that contain {@code /place/} segments.
 *
 * <p><strong>Supported URL patterns:</strong>
 * <ul>
 *   <li>{@code https://www.google.com/maps/@40.7127753,-74.0059728,12z} - Coordinates only</li>
 *   <li>{@code https://www.google.com/maps/place/New+York/@40.7127753,-74.0059728,12z} - Coordinates with place name</li>
 *   <li>{@code https://www.google.com/maps/place/Caf%C3%A9+de+la+Paix/@48.8566,2.3522,17z} - URL-encoded place names</li>
 * </ul>
 *
 * <p><strong>Chain of Responsibility:</strong>
 * This extractor is executed with order 3, after the LatLon3d4dExtractor but before other extractors.
 *
 * <p><strong>Place Name Handling:</strong>
 * When a place name is found in the URL, it will be URL-decoded to handle special characters and spaces.
 * The {@code +} symbols are converted to spaces, and percent-encoded characters are properly decoded.
 */
@Component
@Order(3)
@Slf4j
public class G3AtSymbolExtractor implements GoogleCoordinateExtractor {

    /**
     * Regular expression pattern to match coordinates in the format @latitude,longitude.
     * Supports both positive and negative decimal numbers.
     */
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("@([\\-\\d.]+),([\\-\\d.]+)");

    @Override
    public @NotNull LocationResult extract(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        Coordinate coordinate = extractCoordinate(url);
        if (coordinate == null) {
            return new LocationResult();
        }
        
        String placeName = extractPlaceName(url);
        return new LocationResult(null, coordinate, null, placeName);
    }

    @Nullable
    private Coordinate extractCoordinate(String url) {
        Matcher matcher = COORDINATE_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }

        try {
            double lat = Double.parseDouble(matcher.group(1));
            double lon = Double.parseDouble(matcher.group(2));
            log.debug("Extracted coordinates from @ pattern: {},{}", lat, lon);
            return new Coordinate(lat, lon);
        } catch (NumberFormatException e) {
            log.warn("Invalid coordinate format in @ pattern: {}", url);
            return null;
        }
    }
}