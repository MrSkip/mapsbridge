package com.example.mapsbridge.metrics;

/**
 * Constants class for metric tag names and values used in monitoring.
 */
public final class MetricTags {
    /**
     * Tag key for input type metrics
     */
    public static final String INPUT_TYPE = "type";

    /**
     * Tag key for map provider metrics
     */
    public static final String PROVIDER = "provider";

    /**
     * Tag value for coordinate input type
     */
    public static final String COORDINATES = "coordinates";

    /**
     * Tag value for URL input type
     */
    public static final String URL = "url";

    /**
     * Tag value for unknown provider
     */
    public static final String UNKNOWN = "unknown";

    // Private constructor to prevent instantiation
    private MetricTags() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}