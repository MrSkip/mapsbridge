package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.config.metrics.tracker.MapProviderTracker;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.provider.extractor.google.GoogleCoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Google Maps provider implementation.
 * Uses Chain of Responsibility pattern for coordinate extraction.
 */
@Service
@Slf4j
public class GoogleMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?google\\.com/maps.*|https?://maps\\.google\\.com.*|https?://maps\\.app\\.goo\\.gl/.*|https?://goo\\.gl/maps/.*");

    /**
     * Constructor with dependency injection.
     *
     * @param httpClient  The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     * @param extractors  The list of coordinate extractors
     */
    @Autowired
    public GoogleMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.google.url:https://www.google.com/maps?q={lat},{lon}}") String urlTemplate,
            List<GoogleCoordinateExtractor> extractors,
            MapProviderTracker mapProviderTracker) {
        super(httpClient, urlTemplate, URL_PATTERN, extractors, mapProviderTracker);
    }

    @Override
    public MapType getType() {
        return MapType.GOOGLE;
    }

}
