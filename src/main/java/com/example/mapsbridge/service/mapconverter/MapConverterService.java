package com.example.mapsbridge.service.mapconverter;

import com.example.mapsbridge.dto.request.ConvertRequest;
import com.example.mapsbridge.dto.response.BaseConvertResponse;

/**
 * Interface for converting map URLs and coordinates.
 */
public interface MapConverterService<T extends BaseConvertResponse> {
    
    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     *
     * @param request The request containing input to convert (coordinates or URL)
     * @return A response containing the extracted coordinates and links to all supported map providers
     */
    T convert(ConvertRequest request);
}