package com.example.mapsbridge.provider.impl;

import java.util.regex.Pattern;

import com.example.mapsbridge.provider.AbstractMapProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.OkHttpClient;

import com.example.mapsbridge.dto.MapType;

/**
 * Apple Maps provider implementation.
 */
@Service
public class AppleMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?maps\\.apple\\.com/.*");
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("(?:ul\\?ll=|@?|&coordinate=)(?<lat>-?\\d+\\.\\d+),(?<lon>-?\\d+\\.\\d+)");

    /**
     * Constructor with dependency injection.
     *
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public AppleMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.apple.url}") String urlTemplate) {
        super(httpClient, urlTemplate, URL_PATTERN, COORDINATE_PATTERN);
    }

    @Override
    public MapType getType() {
        return MapType.APPLE;
    }
}
