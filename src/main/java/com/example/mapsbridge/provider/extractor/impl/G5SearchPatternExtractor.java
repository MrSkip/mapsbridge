package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.GoogleCoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that handles /search/LAT,LON URLs in Google Maps.
 */
@Component
@Order(5) // Execute after QParameterExtractor
@Slf4j
public class G5SearchPatternExtractor implements GoogleCoordinateExtractor {

    private static final Pattern SEARCH_PATTERN = Pattern.compile("/search/([-+]?\\d+\\.\\d+),([-+]?\\d+\\.\\d+)");

    @Override
    public @NotNull LocationResult extract(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        Matcher matcher = SEARCH_PATTERN.matcher(url);
        if (matcher.find()) {
            try {
                double lat = Double.parseDouble(matcher.group(1));
                double lon = Double.parseDouble(matcher.group(2));
                log.debug("Extracted coordinates from /search/ pattern: {},{}", lat, lon);
                Coordinate coordinate = new Coordinate(lat, lon);
                return LocationResult.fromCoordinates(coordinate);
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinate format in /search/ pattern: {}", url);
            }
        }

        return new LocationResult();
    }
}
