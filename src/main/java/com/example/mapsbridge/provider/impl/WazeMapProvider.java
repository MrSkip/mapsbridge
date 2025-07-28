package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.config.metrics.tracker.MapProviderTracker;
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

    /**
     * Constructor with dependency injection.
     * 
     * @param webClient The WebClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public WazeMapProvider(
            OkHttpClient webClient,
            @Value("${maps.waze.url:https://waze.com/ul?ll={lat},{lon}&navigate=yes}") String urlTemplate,
            List<WazeCoordinateExtractor> extractors,
            MapProviderTracker mapProviderTracker) {
        super(webClient, urlTemplate, URL_PATTERN, extractors, mapProviderTracker);
    }

    @Override
    public MapType getType() {
        return MapType.WAZE;
    }
}
