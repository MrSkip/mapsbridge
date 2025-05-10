package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.provider.extractor.CoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that parses coordinates from @LAT,LON segments in Google Maps URLs.
 */
@Component
@Order(3) // Execute after LatLon3d4dExtractor
@Slf4j
public class AtSymbolExtractor implements CoordinateExtractor {

    private static final Pattern PATTERN_AT = Pattern.compile("@([\\-\\d.]+),([\\-\\d.]+)");

    @Override
    public Coordinate extract(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = PATTERN_AT.matcher(url);
        if (matcher.find()) {
            try {
                double lat = Double.parseDouble(matcher.group(1));
                double lon = Double.parseDouble(matcher.group(2));
                log.debug("Extracted coordinates from @ pattern: {},{}", lat, lon);
                return new Coordinate(lat, lon);
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinate format in @ pattern: {}", url);
            }
        }

        return null;
    }
}