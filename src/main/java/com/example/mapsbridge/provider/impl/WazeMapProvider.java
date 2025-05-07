package com.example.mapsbridge.provider.impl;

import java.util.regex.Pattern;

import com.example.mapsbridge.provider.AbstractMapProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;

/**
 * Waze Maps provider implementation.
 */
@Service
public class WazeMapProvider extends AbstractMapProvider {

    /**
     * Constructor with dependency injection.
     * 
     * @param webClient The WebClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public WazeMapProvider(
            OkHttpClient webClient,
            @Value("${maps.waze.url:https://waze.com/ul?ll={lat},{lon}&navigate=yes}") String urlTemplate) {
        super(webClient, urlTemplate);

        // Initialize URL patterns
        this.urlPattern = Pattern.compile("https?://(www\\.|ul\\.)?waze\\.com/.*");
        // Handle both standard "ll=" format and live-map "ll." format, plus URL-encoded commas (%2C)
        this.coordinatePattern = Pattern.compile("ll[=.](?<lat>-?\\d+\\.?\\d*)(?:[,]|%2C)(?<lon>-?\\d+\\.?\\d*)");
    }

    @Override
    public MapType getType() {
        return MapType.WAZE;
    }

    @Override
    public Coordinate extractCoordinates(String url) {
        return extractCoordinatesWithRedirects(url);
    }
}
