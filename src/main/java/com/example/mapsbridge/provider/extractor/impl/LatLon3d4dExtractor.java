package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.provider.extractor.CoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that parses coordinates from !3dLAT!4dLON patterns in Google Maps URLs.
 */
@Component
@Order(2) // Execute after RedirectUrlExtractor
@Slf4j
public class LatLon3d4dExtractor implements CoordinateExtractor {

    private static final Pattern PATTERN_3D4D = Pattern.compile("!3d([\\-\\d.]+)!4d([\\-\\d.]+)");

    @Override
    public Coordinate extract(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
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
            return new Coordinate(lastLat, lastLon);
        }

        return null;
    }
}