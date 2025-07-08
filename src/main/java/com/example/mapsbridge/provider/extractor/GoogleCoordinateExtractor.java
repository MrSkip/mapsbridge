package com.example.mapsbridge.provider.extractor;

import com.example.mapsbridge.dto.LocationResult;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for extracting coordinates from a URL.
 * Part of the Chain of Responsibility pattern for coordinate extraction.
 */
public interface GoogleCoordinateExtractor {

    /**
     * Attempts to extract coordinates from the given URL.
     * 
     * @param url The URL to extract coordinates from
     * @return LocationResult object if extraction is successful
     */
    @NotNull
    LocationResult extract(String url);
}
