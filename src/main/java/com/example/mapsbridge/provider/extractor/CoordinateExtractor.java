package com.example.mapsbridge.provider.extractor;

import com.example.mapsbridge.dto.LocationResult;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for extracting coordinates from a URL.
 * Part of the Chain of Responsibility pattern for coordinate extraction.
 */
public interface CoordinateExtractor {
    @NotNull
    LocationResult extract(String url);
}
