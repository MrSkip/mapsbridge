package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.metrics.MapProviderMetrics;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.provider.extractor.apple.AppleCoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

import static com.example.mapsbridge.provider.utils.UrlUtils.encodeUrlParameter;

/**
 * Apple Maps provider implementation.
 * Uses Chain of Responsibility pattern for coordinate extraction.
 */
@Service
@Slf4j
public class AppleMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?maps\\.apple\\.com/.*");
    private static final String APPLE_MAPS_BASE_URL = "https://maps.apple.com/place?";
    private static final String COORDINATE_PARAM = "ll=";
    private static final String ADDRESS_PARAM = "&address=";
    private static final String PLACE_NAME_PARAM = "&q=";

    /**
     * Constructor with dependency injection.
     *
     * @param httpClient  The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     * @param extractors  The list of coordinate extractors
     */
    @Autowired
    public AppleMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.apple.url}") String urlTemplate,
            List<AppleCoordinateExtractor> extractors,
            MapProviderMetrics mapProviderMetrics) {
        super(httpClient, urlTemplate, URL_PATTERN, extractors, mapProviderMetrics);
    }

    @Override
    public MapType getType() {
        return MapType.APPLE;
    }

    @Override
    public String generateUrl(LocationResult location) {
        validateLocation(location);

        if (shouldUseSimpleCoordinateUrl(location)) {
            return super.generateUrl(location);
        }

        return buildRichAppleUrl(location);
    }

    /**
     * Determines if we should use the simple coordinate-only URL format.
     */
    private boolean shouldUseSimpleCoordinateUrl(LocationResult location) {
        return !location.hasValidAddress() && !location.hasValidPlaceName();
    }

    /**
     * Builds a rich Apple Maps URL with coordinates, address, and place name.
     */
    private String buildRichAppleUrl(LocationResult location) {
        StringBuilder urlBuilder = new StringBuilder(APPLE_MAPS_BASE_URL);

        // Add coordinates
        urlBuilder.append(COORDINATE_PARAM)
                .append(location.getCoordinateParam());

        // Add address if present
        if (location.hasValidAddress()) {
            urlBuilder.append(ADDRESS_PARAM)
                    .append(encodeUrlParameter(location.getAddress()));
        }

        // Add place name if present
        if (location.hasValidPlaceName()) {
            urlBuilder.append(PLACE_NAME_PARAM)
                    .append(encodeUrlParameter(location.getPlaceName()));
        }

        String generatedUrl = urlBuilder.toString();
        log.debug("Generated Apple Maps URL: {}", generatedUrl);

        return generatedUrl;
    }
}