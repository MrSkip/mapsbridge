package com.example.mapsbridge.provider.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.mapsbridge.provider.AbstractMapProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.OkHttpClient;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;

/**
 * Bing Maps provider implementation.
 */
@Service
public class BingMapProvider extends AbstractMapProvider {

    /**
     * Constructor with dependency injection.
     * 
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public BingMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.bing.url:https://www.bing.com/maps?q={lat},{lon}}") String urlTemplate) {
        super(httpClient, urlTemplate);
        // Initialize URL patterns
        this.urlPattern = Pattern.compile("https?://(www\\.)?bing\\.com/maps.*");
        this.coordinatePattern = Pattern.compile(
                "q=(?<lat>-?\\d{1,3}[.,]\\d+)[,](?<lon>-?\\d{1,3}[.,]\\d+)" +
                        "|cp=(?<lat2>-?\\d{1,3}\\.\\d+)~(?<lon2>-?\\d{1,3}\\.\\d+)"
        );;
    }

    @Override
    public MapType getType() {
        return MapType.BING;
    }
}
