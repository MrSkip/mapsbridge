package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.provider.extractor.apple.AppleCoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Apple Maps provider implementation.
 * Uses Chain of Responsibility pattern for coordinate extraction.
 */
@Service
@Slf4j
public class AppleMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?maps\\.apple\\.com/.*");

    /**
     * Constructor with dependency injection.
     *
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     * @param extractors The list of coordinate extractors
     */
    @Autowired
    public AppleMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.apple.url}") String urlTemplate,
            List<AppleCoordinateExtractor> extractors) {
        super(httpClient, urlTemplate, URL_PATTERN, extractors);
        this.extractors = extractors;
    }

    @Override
    public MapType getType() {
        return MapType.APPLE;
    }
}
