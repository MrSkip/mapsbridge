package com.example.mapsbridge.provider.extractor;

import com.example.mapsbridge.model.Coordinate;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for extracting coordinates from a URL.
 * Part of the Chain of Responsibility pattern for coordinate extraction.
 */
public interface CoordinateExtractor {

    /**
     * Attempts to extract coordinates from the given URL.
     * 
     * @param url The URL to extract coordinates from
     * @return Coordinate object if extraction is successful, null otherwise
     */
    @Nullable
    Coordinate extract(String url);
}
