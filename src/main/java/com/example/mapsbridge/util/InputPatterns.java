package com.example.mapsbridge.util;

import java.util.regex.Pattern;

/**
 * Utility class containing patterns for validating input formats.
 */
public final class InputPatterns {
    /**
     * Pattern for validating coordinate input in various formats:
     * - "lat,lon" (comma-separated)
     * - "lat , lon" (comma with spaces)
     * - "lat lon" (space-separated)
     * - "lat    lon" (multiple spaces)
     * Examples: "40.6892,-74.0445", "40.6892 -74.0445", "51.5074, -0.1278"
     */
    public static final Pattern COORDINATE_PATTERN = Pattern.compile("^-?\\d+\\.?\\d*[,\\s]\\s*-?\\d+\\.?\\d*$");

    /**
     * Pattern for validating URL input
     * Examples: "https://maps.google.com", "http://www.openstreetmap.org"
     */
    public static final Pattern URL_PATTERN = Pattern.compile("^https?://.*");

    /**
     * Pattern for extracting URL from text
     * Examples: "Some text https://maps.google.com more text", "Cantina Charlotta\nMap Item\nhttps://maps.apple.com/place?address=..."
     */
    public static final Pattern URL_EXTRACTION_PATTERN = Pattern.compile("https?://[^\\s\\n\\r]+");

    // Private constructor to prevent instantiation
    private InputPatterns() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}