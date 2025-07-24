package com.example.mapsbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Response model for the map link conversion API.
 * Contains the extracted location information and links to various map providers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertResponse {

    /**
     * The geographic coordinates extracted from the input.
     */
    private Coordinate coordinates;

    private String name;

    private String address;

    /**
     * Map of links to different map providers.
     * Key: provider type (e.g., MapType.GOOGLE, MapType.APPLE)
     * Value: URL for that provider
     */
    private Map<MapType, String> links = new HashMap<>();

    /**
     * Adds a link for a specific map provider.
     *
     * @param providerType The type of the map provider
     * @param url          The URL for that provider
     */
    public void addLink(MapType providerType, String url) {
        links.put(providerType, url);
    }
}
