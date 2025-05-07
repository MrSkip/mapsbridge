package com.example.mapsbridge.provider.impl;

import java.util.regex.Pattern;

import com.example.mapsbridge.provider.AbstractMapProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.OkHttpClient;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;

/**
 * OpenStreetMap provider implementation.
 */
@Service
public class OpenStreetMapProvider extends AbstractMapProvider {

    /**
     * Constructor with dependency injection.
     * 
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     */
    public OpenStreetMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.osm.url:https://www.openstreetmap.org/?mlat={lat}&mlon={lon}}") String urlTemplate) {
        super(httpClient, urlTemplate);

        // Initialize URL patterns
        this.urlPattern = Pattern.compile("https?://(www\\.)?openstreetmap\\.org/.*");
        this.coordinatePattern = Pattern.compile(
                "mlat=(?<lat>-?\\d+(?:\\.\\d+)?)&mlon=(?<lon>-?\\d+(?:\\.\\d+)?)" +
                        "|#map=\\d+/(?<lat2>-?\\d+(?:\\.\\d+)?)/(?<lon2>-?\\d+(?:\\.\\d+)?)"
        );
    }

    @Override
    public MapType getType() {
        return MapType.OPENSTREETMAP;
    }

    @Override
    public Coordinate extractCoordinates(String url) {
        return extractCoordinatesWithRedirects(url);
    }
}
