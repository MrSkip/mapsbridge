package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.provider.extractor.CoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
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
public class SearchPatternExtractor implements CoordinateExtractor {

    private static final Pattern SEARCH_PATTERN = Pattern.compile("/search/([-+]?\\d+\\.\\d+),([-+]?\\d+\\.\\d+)");

    @Override
    public Coordinate extract(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = SEARCH_PATTERN.matcher(url);
        if (matcher.find()) {
            try {
                double lat = Double.parseDouble(matcher.group(1));
                double lon = Double.parseDouble(matcher.group(2));
                log.debug("Extracted coordinates from /search/ pattern: {},{}", lat, lon);
                return new Coordinate(lat, lon);
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinate format in /search/ pattern: {}", url);
            }
        }

        return null;
    }
}