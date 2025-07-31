package com.example.mapsbridge.service.mapconverter;

import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.dto.request.ConvertRequest;
import com.example.mapsbridge.dto.response.WebConvertResponse;
import com.example.mapsbridge.provider.MapProvider;
import com.example.mapsbridge.service.UserInputProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for converting map URLs and coordinates.
 * Implements MapConverterService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapConverterServiceImpl implements MapConverterService<WebConvertResponse> {

    private final List<MapProvider> mapProviders;
    private final UserInputProcessorService userInputProcessorService;

    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     *
     * @param request The request containing input to convert (coordinates or URL)
     * @return A response containing the extracted location information and links to all supported map providers
     */
    @Override
    public WebConvertResponse convert(ConvertRequest request) {
        String input = request.getInput().trim();
        LocationResult locationResult = userInputProcessorService.processInput(input);
        return getWebConvertResponse(request, locationResult);
    }

    private WebConvertResponse getWebConvertResponse(ConvertRequest request, LocationResult locationResult) {
        WebConvertResponse response = new WebConvertResponse();
        response.setCoordinates(locationResult.getCoordinates());
        response.setAddress(locationResult.getAddress());
        response.setName(locationResult.getPlaceName());
        response.setLinks(generateMapLinks(locationResult, request.getInput()));
        return response;
    }

    private Map<MapType, String> generateMapLinks(LocationResult locationResult, String userInput) {
        Map<MapType, String> links = new HashMap<>();
        for (MapProvider provider : mapProviders) {
            if (provider.getType().equals(locationResult.getMapSource())) {
                links.put(provider.getType(), userInput);
                continue;
            }
            try {
                String url = provider.generateUrl(locationResult);
                links.put(provider.getType(), url);
            } catch (Exception e) {
                log.error("Error generating URL for provider {}: {}",
                        provider.getType().getName(), e.getMessage());
            }
        }
        return links;
    }

}

