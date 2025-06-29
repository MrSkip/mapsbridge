package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.provider.extractor.CoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that parses coordinates from q=LAT,LON parameters in Google Maps URLs.
 */
@Component
@Order(4) // Execute after AtSymbolExtractor
@Slf4j
public class QParameterExtractor implements CoordinateExtractor {

    // Pattern to match both standard decimal format (40.7128,-74.0060) and European format (40,7128,-74,0060)
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("q=(?<lat>-?\\d+[.,]?\\d*),(?<lon>-?\\d+[.,]?\\d*)");

    @Override
    public Coordinate extract(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        // URL decode the string to handle encoded characters
        String decodedUrl;
        try {
            decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Error decoding URL: {}", url, e);
            decodedUrl = url;
        }

        Matcher matcher = COORDINATE_PATTERN.matcher(decodedUrl);
        if (matcher.find()) {
            try {
                String latStr = matcher.group("lat");
                String lonStr = matcher.group("lon");

                // Normalize European decimal commas to dots
                latStr = latStr.replace(',', '.');
                lonStr = lonStr.replace(',', '.');

                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);

                log.debug("Extracted coordinates from q parameter: {},{}", lat, lon);
                return new Coordinate(lat, lon);
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinate format in q parameter: {}", url);
            }
        }

        return null;
    }
}
