package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.provider.extractor.bing.BingCoordinateExtractor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Bing Maps provider implementation.
 */
@Service
public class BingMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?bing\\.com/maps.*");

    /**
     * Constructor with dependency injection.
     *
     * @param httpClient  The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public BingMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.bing.url:https://www.bing.com/maps?q={lat},{lon}}") String urlTemplate,
            List<BingCoordinateExtractor> extractors) {
        super(httpClient, urlTemplate, URL_PATTERN, extractors);
    }

    @Override
    public MapType getType() {
        return MapType.BING;
    }
}
