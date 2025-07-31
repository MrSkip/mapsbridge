package com.example.mapsbridge.service.mapconverter;

import com.example.mapsbridge.config.properties.ShortcutProperties;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.dto.request.ConvertRequest;
import com.example.mapsbridge.dto.response.shortcut.ShortcutBadResponse;
import com.example.mapsbridge.dto.response.shortcut.ShortcutBaseResponse;
import com.example.mapsbridge.dto.response.shortcut.ShortcutResponse;
import com.example.mapsbridge.provider.MapProvider;
import com.example.mapsbridge.service.UserInputProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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
public class ShortcutConverterServiceImpl implements MapConverterService<ShortcutBaseResponse> {

    private final List<MapProvider> mapProviders;
    private final UserInputProcessorService userInputProcessorService;
    private final ShortcutProperties shortcutProperties;

    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     *
     * @param request The request containing input to convert (coordinates or URL)
     * @return A response containing the extracted location information and links to all supported map providers
     */
    @Override
    public ShortcutBaseResponse convert(ConvertRequest request) {
        try {
            String input = request.getInput().trim();
            if (StringUtils.isBlank(input)) {
                return createBadResponse();
            }
            LocationResult locationResult = userInputProcessorService.processInput(input);
            return getWebConvertResponse(locationResult);
        } catch (Exception e) {
            log.error("Error converting input: {}", request, e);
            return createBadResponse();
        }
    }

    private @NotNull ShortcutBadResponse createBadResponse() {
        ShortcutProperties.BadResponse badResponseProps = shortcutProperties.getBadResponse();
        return new ShortcutBadResponse(
                badResponseProps.getAlertTitle(),
                badResponseProps.getAlertMessage(),
                badResponseProps.getUrl()
        );
    }

    private ShortcutBaseResponse getWebConvertResponse(LocationResult locationResult) {
        ShortcutResponse response = new ShortcutResponse();
        Map<MapType, String> links = generateMapLinks(locationResult);
        response.setProviders(shortcutProperties.getMapProviders().createProvidersMap(links));
        return response;
    }

    private Map<MapType, String> generateMapLinks(LocationResult locationResult) {
        Map<MapType, String> links = new HashMap<>();
        for (MapProvider provider : mapProviders) {
            if (provider.getType().equals(locationResult.getMapSource())) {
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
        // Apply the configured order to the map
        return shortcutProperties.getMapProviders().createOrderedMap(links);
    }

}

