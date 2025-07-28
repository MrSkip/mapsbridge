package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.config.metrics.tracker.MapProviderTracker;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.provider.AbstractMapProvider;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Komoot Maps provider implementation.
 */
@Service
public class KomootMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?komoot\\.com/.*");

    /**
     * Constructor with dependency injection.
     * 
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public KomootMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.komoot.url}") String urlTemplate,
            MapProviderTracker mapProviderTracker) {
        super(httpClient, urlTemplate, URL_PATTERN, List.of(), mapProviderTracker);
    }

    @Override
    public MapType getType() {
        return MapType.KOMOOT;
    }

    @Override
    public LocationResult extractLocation(String url) {
        // not supported
        return new LocationResult();
    }
}
