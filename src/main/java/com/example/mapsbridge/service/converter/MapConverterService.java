package com.example.mapsbridge.service.converter;

import com.example.mapsbridge.dto.request.ConvertRequest;

/**
 * Interface for converting map URLs and coordinates.
 */
public interface MapConverterService<T> {
    
    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     *
     * @param request The request containing input to convert (coordinates or URL)
     * @return A response containing the extracted coordinates and links to all supported map providers
     */
    T convert(ConvertRequest request);
}