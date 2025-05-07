package com.example.mapsbridge.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing all supported map providers.
 * This provides a type-safe way to identify map providers throughout the application.
 */
public enum MapType {
    GOOGLE("google"),
    APPLE("apple"),
    BING("bing"),
    OPENSTREETMAP("osm"),
    WAZE("waze");

    private final String name;

    /**
     * Constructor for MapType enum.
     * 
     * @param name The string identifier for the map provider
     */
    MapType(String name) {
        this.name = name;
    }

    /**
     * Get the string identifier for the map provider.
     * 
     * @return The provider name as a string
     */
    @JsonValue
    public String getName() {
        return name;
    }

    /**
     * Find a MapType by its string identifier.
     * 
     * @param providerName The string identifier to look for
     * @return The corresponding MapType, or null if not found
     */
    public static MapType fromString(String providerName) {
        if (providerName == null) {
            return null;
        }

        for (MapType mapType : MapType.values()) {
            if (mapType.name.equalsIgnoreCase(providerName)) {
                return mapType;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
