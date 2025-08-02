package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.config.metrics.MetricTags;
import com.example.mapsbridge.config.metrics.tracker.MapProviderTracker;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.provider.extractor.waze.WazeCoordinateExtractor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Waze Maps provider implementation.
 */
@Service
public class WazeMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.|ul\\.)?waze\\.com/.*");
    private final String shortcutUrlTemplate;

    /**
     * Constructor with dependency injection.
     * 
     * @param webClient The WebClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     * @param shortcutUrlTemplate The URL template for shortcuts from configuration
     */
    public WazeMapProvider(
            OkHttpClient webClient,
            @Value("${maps.waze.url}") String urlTemplate,
            @Value("${maps.waze.shortcut.url}") String shortcutUrlTemplate,
            List<WazeCoordinateExtractor> extractors,
            MapProviderTracker mapProviderTracker) {
        super(webClient, urlTemplate, URL_PATTERN, extractors, mapProviderTracker);
        this.shortcutUrlTemplate = shortcutUrlTemplate;
    }

    @Override
    public String generateUrl(LocationResult location) {
        String endpointType = LoggingContext.getEndpointType();
        if (MetricTags.SHORTCUT.equalsIgnoreCase(endpointType)) {
            return buildFinalUrl(location, shortcutUrlTemplate);
        }
        return super.generateUrl(location);
    }

    @Override
    public MapType getType() {
        return MapType.WAZE;
    }
}
