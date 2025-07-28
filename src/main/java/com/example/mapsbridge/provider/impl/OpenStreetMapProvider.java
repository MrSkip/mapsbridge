package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.config.metrics.tracker.MapProviderTracker;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.provider.extractor.openstreet.OpenStreetMapCoordinateExtractor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * OpenStreetMap provider implementation.
 */
@Service
public class OpenStreetMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?openstreetmap\\.org/.*");

    /**
     * Constructor with dependency injection.
     * 
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public OpenStreetMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.osm.url:https://www.openstreetmap.org/?mlat={lat}&mlon={lon}}") String urlTemplate,
            List<OpenStreetMapCoordinateExtractor> extractors,
            MapProviderTracker mapProviderTracker) {
        super(httpClient, urlTemplate, URL_PATTERN, extractors, mapProviderTracker);
    }

    @Override
    public MapType getType() {
        return MapType.OPENSTREETMAP;
    }

}
