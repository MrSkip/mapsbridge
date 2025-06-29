package com.example.mapsbridge.provider.impl;

import java.util.regex.Pattern;

import com.example.mapsbridge.provider.AbstractMapProvider;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.mapsbridge.dto.MapType;

/**
 * Waze Maps provider implementation.
 */
@Service
public class WazeMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.|ul\\.)?waze\\.com/.*");
    // Handle both standard "ll=" format and live-map "ll." format, plus URL-encoded commas (%2C)
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("ll[=.](?<lat>-?\\d+\\.?\\d*)(?:[,]|%2C)(?<lon>-?\\d+\\.?\\d*)");

    /**
     * Constructor with dependency injection.
     * 
     * @param webClient The WebClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public WazeMapProvider(
            OkHttpClient webClient,
            @Value("${maps.waze.url:https://waze.com/ul?ll={lat},{lon}&navigate=yes}") String urlTemplate) {
        super(webClient, urlTemplate, URL_PATTERN, COORDINATE_PATTERN);
    }

    @Override
    public MapType getType() {
        return MapType.WAZE;
    }
}
