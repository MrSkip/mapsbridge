package com.example.mapsbridge.provider.extractor.impl.url;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to handle URL pattern extraction logic.
 * This separates URL parsing concerns from the main service logic.
 */
@Slf4j
@Component
public class UrlPatternExtractor {

    // Patterns for finding place IDs, coordinates, and addresses
    private static final Pattern PLACE_ID_PATTERN = Pattern.compile("place_id=([\\w\\-]+)");
    private static final Pattern PLACE_ID_PATTERN_2 = Pattern.compile("!1s([\\w\\-:]+)");
    private static final Pattern PLACE_ID_PATTERN_3 = Pattern.compile("!3m\\d+!1s([\\w\\-:]+)");
    private static final Pattern QUERY_PATTERN = Pattern.compile("q=([^&]+)");

    private static final Pattern[] PLACE_ID_PATTERNS = {
            PLACE_ID_PATTERN, PLACE_ID_PATTERN_2, PLACE_ID_PATTERN_3
    };

    /**
     * Finds a place ID in the given URL.
     *
     * @param url The URL to search for a place ID
     * @return An Optional containing the place ID if found, or empty if not found
     */
    public Optional<String> findPlaceId(String url) {
        for (Pattern pattern : PLACE_ID_PATTERNS) {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String placeId = matcher.group(1);
                log.debug("Extracted place_id with pattern {}: {}", pattern.pattern(), placeId);
                return Optional.of(placeId);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds an address query in the given URL.
     *
     * @param url The URL to search for an address query
     * @return An Optional containing the address query if found, or empty if not found
     */
    public Optional<String> findAddressQuery(String url) {
        Matcher queryMatcher = QUERY_PATTERN.matcher(url);
        if (queryMatcher.find()) {
            String query = queryMatcher.group(1);
            try {
                query = URLDecoder.decode(query, StandardCharsets.UTF_8);
                log.debug("Extracted query: {}", query);
                return Optional.of(query);
            } catch (Exception e) {
                log.warn("Error decoding query: {}", query, e);
                // Fallback to manual replacement if decoding fails
                query = query.replace("+", " ");
                return Optional.of(query);
            }
        }
        return Optional.empty();
    }
}
