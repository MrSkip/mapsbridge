package com.example.mapsbridge.provider.impl;

import java.util.regex.Pattern;

import com.example.mapsbridge.provider.AbstractMapProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.OkHttpClient;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;

/**
 * Google Maps provider implementation.
 */
@Service
@Slf4j
public class GoogleMapProvider extends AbstractMapProvider {

    private final GeoApiContext geoApiContext;
    private final boolean apiEnabled;

    /**
     * Constructor with dependency injection.
     * 
     * @param httpClient The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     * @param googleApiKey The Google API key from configuration
     */
    public GoogleMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.google.url:https://www.google.com/maps?q={lat},{lon}}") String urlTemplate,
            @Value("${google.api.key:}") String googleApiKey) {
        super(httpClient, urlTemplate);

        // Initialize URL patterns
        this.urlPattern = Pattern.compile("https?://(www\\.)?google\\.com/maps.*");
        this.coordinatePattern = Pattern.compile("q=(?<lat>-?\\d+\\.?\\d*),(?<lon>-?\\d+\\.?\\d*)");

        // Initialize Google API context if key is provided
        this.apiEnabled = googleApiKey != null && !googleApiKey.isEmpty();
        this.geoApiContext = apiEnabled ? new GeoApiContext.Builder().apiKey(googleApiKey).build() : null;
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

        // First try to extract coordinates from the URL directly
        Coordinate coordinate = super.extractCoordinates(url);
        if (coordinate != null) {
            return coordinate;
        }

        // If not found, follow redirects and try again
        String finalUrl = followRedirects(url);
        if (!url.equals(finalUrl)) {
            coordinate = super.extractCoordinates(finalUrl);
            if (coordinate != null) {
                return coordinate;
            }
        }

        // If still not found and Google API is enabled, try to get coordinates from place ID
        if (apiEnabled) {
            try {
                // Extract place ID from URL
                Pattern placeIdPattern = Pattern.compile("place_id=([\\w\\-]+)");
                java.util.regex.Matcher matcher = placeIdPattern.matcher(finalUrl);
                if (matcher.find()) {
                    String placeId = matcher.group(1);

                    // Get place details from Google API
                    PlaceDetailsRequest request = PlacesApi.placeDetails(geoApiContext, placeId);
                    PlaceDetails details = request.await();

                    if (details != null && details.geometry != null && details.geometry.location != null) {
                        LatLng location = details.geometry.location;
                        return new Coordinate(location.lat, location.lng);
                    }
                }
            } catch (Exception e) {
                // Log error but continue
                log.error("Error getting coordinates from Google API", e);
            }
        }

        return null;
    }
}
