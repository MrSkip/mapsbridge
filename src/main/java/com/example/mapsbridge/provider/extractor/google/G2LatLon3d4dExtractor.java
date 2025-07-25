package com.example.mapsbridge.provider.extractor.google;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.mapsbridge.provider.utils.PlaceNameDecoder.extractPlaceName;

/**
 * Extractor that parses coordinates from !3dLAT!4dLON patterns in Google Maps URLs.
 *
 * <p>This extractor implements the {@link GoogleCoordinateExtractor} interface to extract
 * geographic coordinates from Google Maps URLs that contain the !3d!4d pattern format.
 * The pattern !3d represents latitude and !4d represents longitude in Google's URL encoding.</p>
 *
 * <p>The extractor processes URLs containing patterns like:
 * <ul>
 *   <li>!3d40.7128!4d-74.0060 (New York City coordinates)</li>
 *   <li>!3d51.5074!4d-0.1278 (London coordinates)</li>
 * </ul>
 * </p>
 *
 * <p>If multiple coordinate patterns are found in a single URL, the extractor returns
 * the last occurrence, which typically represents the most relevant location.</p>
 */

@Component
@Order(2)
@Slf4j
public class G2LatLon3d4dExtractor implements GoogleCoordinateExtractor {

    private static final String COUNTER_TAG_METHOD = "latLon3d4d";

    /**
     * Regular expression pattern to match !3d!4d coordinate format in Google Maps URLs.
     *
     * <p>Pattern breakdown:
     * <ul>
     *   <li>!3d - literal string indicating latitude parameter</li>
     *   <li>([\\-\\d.]+) - capturing group for latitude value (supports negative numbers and decimals)</li>
     *   <li>!4d - literal string indicating longitude parameter</li>
     *   <li>([\\-\\d.]+) - capturing group for longitude value (supports negative numbers and decimals)</li>
     * </ul>
     * </p>
     */
    private static final Pattern PATTERN_3D4D = Pattern.compile("!3d([\\-\\d.]+)!4d([\\-\\d.]+)");

    private final Counter latLon3d4dCounter;

    @Autowired
    public G2LatLon3d4dExtractor(
            Counter.Builder googleMapsExtractorCounterBuilder,
            MeterRegistry meterRegistry) {
        this.latLon3d4dCounter = googleMapsExtractorCounterBuilder
                .tag("method", COUNTER_TAG_METHOD)
                .register(meterRegistry);
    }

    @Override
    public @NotNull LocationResult extract(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        Matcher matcher = PATTERN_3D4D.matcher(url);
        double lastLat = 0.0;
        double lastLon = 0.0;
        boolean found = false;

        // Find the last occurrence of the pattern in the URL
        while (matcher.find()) {
            try {
                lastLat = Double.parseDouble(matcher.group(1));
                lastLon = Double.parseDouble(matcher.group(2));
                found = true;
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinate format in !3d!4d pattern: {}", url);
            }
        }

        if (found) {
            log.debug("Extracted coordinates from !3d!4d pattern: {},{}", lastLat, lastLon);
            Coordinate coordinate = new Coordinate(lastLat, lastLon);

            String placeName = extractPlaceName(url);

            // Increment counter for successful coordinate extraction
            latLon3d4dCounter.increment();

            return new LocationResult(null, coordinate, null, placeName);
        }

        return new LocationResult();
    }
}
