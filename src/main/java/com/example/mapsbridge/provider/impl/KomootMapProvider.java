package com.example.mapsbridge.provider.impl;

import java.util.regex.Pattern;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.provider.AbstractMapProvider;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.mapsbridge.model.MapType;

/**
 * Komoot Maps provider implementation.
 */
@Service
public class KomootMapProvider extends AbstractMapProvider {

    /**
     * Constructor with dependency injection.
     * 
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public KomootMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.komoot.url}") String urlTemplate) {
        super(httpClient, urlTemplate);

        // Initialize URL patterns
        this.urlPattern = Pattern.compile("https?://(www\\.)?komoot\\.com/.*");
        // Pattern to extract coordinates from Komoot URLs
        this.coordinatePattern = Pattern.compile("@(?<lat>-?\\d+\\.?\\d*),(?<lon>-?\\d+\\.?\\d*)");
    }

    @Override
    public MapType getType() {
        return MapType.KOMOOT;
    }

    @Override
    public Coordinate extractCoordinates(String url) {
        // not supported
        return null;
    }
}