package com.example.mapsbridge.config.metrics;

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
     * Tag key for request source metrics
     */
    public static final String CLIENT_SOURCE = "source";

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

    /**
     * Tag value for API request source
     */
    public static final String API = "API";

    /**
     * Tag value for WEB request source
     */
    public static final String WEB = "WEB";

    /**
     * Tag value for TELEGRAM request source
     */
    public static final String TELEGRAM = "TELEGRAM";

    // Private constructor to prevent instantiation
    private MetricTags() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}