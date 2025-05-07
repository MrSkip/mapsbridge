package com.example.mapsbridge.provider;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;

/**
 * Interface for map providers that can generate location URLs.
 */
public interface MapProvider {

    /**
     * Get the type of the map provider.
     * 
     * @return The provider type as a MapType enum
     */
    MapType getType();

    /**
     * Generate a map URL for the given coordinates.
     * 
     * @param coordinate The coordinates to generate a URL for
     * @return A URL that will open the specified location in this map provider
     */
    String generateUrl(Coordinate coordinate);

    /**
     * Check if a URL is from this map provider.
     * 
     * @param url The URL to check
     * @return true if the URL is from this provider, false otherwise
     */
    boolean isProviderUrl(String url);

    /**
     * Extract coordinates from a URL if possible.
     * 
     * @param url The URL to extract coordinates from
     * @return The extracted coordinates, or null if coordinates couldn't be extracted
     */
    Coordinate extractCoordinates(String url);
}
