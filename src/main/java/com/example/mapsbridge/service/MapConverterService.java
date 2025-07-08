package com.example.mapsbridge.service;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.ConvertResponse;

/**
 * Interface for converting map URLs and coordinates.
 */
public interface MapConverterService {
    
    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     *
     * @param request The request containing input to convert (coordinates or URL)
     * @return A response containing the extracted coordinates and links to all supported map providers
     */
    ConvertResponse convert(ConvertRequest request);
}