package com.example.mapsbridge.provider.impl;

import java.util.regex.Pattern;

import com.example.mapsbridge.provider.AbstractMapProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.OkHttpClient;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;

/**
 * Apple Maps provider implementation.
 */
@Service
public class AppleMapProvider extends AbstractMapProvider {

    /**
     * Constructor with dependency injection.
     *
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public AppleMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.apple.url}") String urlTemplate) {
        super(httpClient, urlTemplate);

        // Initialize URL patterns
        this.urlPattern = Pattern.compile("https?://(www\\.)?maps\\.apple\\.com/.*");
        this.coordinatePattern = Pattern.compile("(?:ul\\?ll=|@?|&coordinate=)(?<lat>-?\\d+\\.\\d+),(?<lon>-?\\d+\\.\\d+)");
    }

    @Override
    public MapType getType() {
        return MapType.APPLE;
    }
}
