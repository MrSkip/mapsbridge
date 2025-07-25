package com.example.mapsbridge.service.impl;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.ConvertResponse;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.MapProvider;
import com.example.mapsbridge.service.MapConverterService;
import com.example.mapsbridge.service.UserInputProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for converting map URLs and coordinates.
 * Implements MapConverterServiceInterface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapConverterServiceImpl implements MapConverterService {

    private final List<MapProvider> mapProviders;
    private final UserInputProcessorService userInputProcessorService;

    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     *
     * @param request The request containing input to convert (coordinates or URL)
     * @return A response containing the extracted location information and links to all supported map providers
     */
    @Override
    public ConvertResponse convert(ConvertRequest request) {
        String input = request.getInput().trim();
        LocationResult locationResult = userInputProcessorService.processInput(input);
        return generateResponse(locationResult);
    }

    /**
     * Generate a response with links to all supported map providers.
     *
     * @param locationResult The location result to use for generating links
     * @return A response containing the location information and links
     */
    private ConvertResponse generateResponse(LocationResult locationResult) {
        ConvertResponse response = new ConvertResponse();
        response.setCoordinates(locationResult.getCoordinates());
        response.setAddress(locationResult.getAddress());
        response.setName(locationResult.getPlaceName());

        for (MapProvider provider : mapProviders) {
            addProviderLink(response, provider, locationResult);
        }

        return response;
    }

    /**
     * Add a link for a specific map provider to the response.
     *
     * @param response   The response to add the link to
     * @param provider   The map provider to generate a link for
     */
    private void addProviderLink(ConvertResponse response, MapProvider provider, LocationResult locationResult) {
        try {
            String url = provider.generateUrl(locationResult);
            response.addLink(provider.getType(), url);
        } catch (Exception e) {
            log.error("Error generating URL for provider {}: {}",
                    provider.getType().getName(), e.getMessage());
        }
    }
}
