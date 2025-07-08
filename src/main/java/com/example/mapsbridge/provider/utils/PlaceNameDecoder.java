package com.example.mapsbridge.provider.utils;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PlaceNameDecoder {
    /**
     * Regular expression pattern to match place names in /place/ URLs.
     * Captures everything between /place/ and the next / or @ symbol.
     */
    private static final Pattern PLACE_NAME_PATTERN = Pattern.compile("/place/([^/@]+)");

    @Nullable
    public static String extractPlaceName(String url) {
        Matcher placeMatcher = PLACE_NAME_PATTERN.matcher(url);
        if (!placeMatcher.find()) {
            return null;
        }

        String encodedPlaceName = placeMatcher.group(1);
        return decodePlaceName(encodedPlaceName);
    }

    @Nullable
    private static String decodePlaceName(String encodedPlaceName) {
        try {
            String decoded = URLDecoder.decode(encodedPlaceName.replace("+", " "), StandardCharsets.UTF_8);
            log.debug("Decoded place name: {} -> {}", encodedPlaceName, decoded);
            return decoded;
        } catch (Exception e) {
            log.warn("Failed to decode place name: {}", encodedPlaceName, e);
            // Return the raw name with + replaced by spaces if decoding fails
            return encodedPlaceName.replace("+", " ");
        }
    }
}
