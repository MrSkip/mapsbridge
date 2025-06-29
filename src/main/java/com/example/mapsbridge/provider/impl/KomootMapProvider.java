package com.example.mapsbridge.provider.impl;

import java.util.regex.Pattern;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.provider.AbstractMapProvider;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.mapsbridge.dto.MapType;

/**
 * Komoot Maps provider implementation.
 */
@Service
public class KomootMapProvider extends AbstractMapProvider {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?komoot\\.com/.*");
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("@(?<lat>-?\\d+\\.?\\d*),(?<lon>-?\\d+\\.?\\d*)");

    /**
     * Constructor with dependency injection.
     * 
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public KomootMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.komoot.url}") String urlTemplate) {
        super(httpClient, urlTemplate, URL_PATTERN, COORDINATE_PATTERN);
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
