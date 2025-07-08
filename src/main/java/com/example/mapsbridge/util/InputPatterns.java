package com.example.mapsbridge.util;

import java.util.regex.Pattern;

/**
 * Utility class containing patterns for validating input formats.
 */
public final class InputPatterns {
    /**
     * Pattern for validating coordinate input in the format "lat,lon"
     * Examples: "40.6892,-74.0445", "51.5074,-0.1278"
     */
    public static final Pattern COORDINATE_PATTERN = Pattern.compile("^-?\\d+\\.?\\d*,-?\\d+\\.?\\d*$");

    /**
     * Pattern for validating URL input
     * Examples: "https://maps.google.com", "http://www.openstreetmap.org"
     */
    public static final Pattern URL_PATTERN = Pattern.compile("^https?://.*");

    // Private constructor to prevent instantiation
    private InputPatterns() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}