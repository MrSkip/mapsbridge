package com.example.mapsbridge.provider.impl;

import java.util.List;
import java.util.regex.Pattern;

import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.provider.extractor.CoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.OkHttpClient;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;

/**
 * Google Maps provider implementation.
 * Uses Chain of Responsibility pattern for coordinate extraction.
 */
@Service
@Slf4j
public class GoogleMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?google\\.com/maps.*|https?://maps\\.app\\.goo\\.gl/.*|https?://goo\\.gl/maps/.*");
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("q=(?<lat>-?\\d+\\.?\\d*),(?<lon>-?\\d+\\.?\\d*)");

    private final List<CoordinateExtractor> extractors;

    /**
     * Constructor with dependency injection.
     * 
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     * @param extractors The list of coordinate extractors
     */
    @Autowired
    public GoogleMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.google.url:https://www.google.com/maps?q={lat},{lon}}") String urlTemplate,
            List<CoordinateExtractor> extractors) {
        super(httpClient, urlTemplate, URL_PATTERN, COORDINATE_PATTERN);
        this.extractors = extractors;
    }

    @Override
    public MapType getType() {
        return MapType.GOOGLE;
    }

    @Override
    public Coordinate extractCoordinates(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        String finalUrl = getResolvedUrlOrDefault(url);

        // Apply each extractor in the chain until one returns a non-null result
        for (CoordinateExtractor extractor : extractors) {
            Coordinate coordinate = extractor.extract(finalUrl);
            if (coordinate != null) {
                log.debug("Extracted coordinates using {}: {},{}", 
                    extractor.getClass().getSimpleName(), coordinate.getLat(), coordinate.getLon());
                return coordinate;
            }
        }

        return null;
    }

    private String getResolvedUrlOrDefault(String url) {
        if (url.startsWith("https://maps.app.goo.gl/") ||
                url.startsWith("https://goo.gl/maps/")) {
            log.debug("Resolving shortened URL: {}", url);
            return followRedirects(url);
        }
        return url;
    }

}
