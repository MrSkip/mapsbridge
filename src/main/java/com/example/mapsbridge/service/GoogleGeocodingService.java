package com.example.mapsbridge.service;

import com.example.mapsbridge.dto.Coordinate;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for handling Google Geocoding API requests.
 */
@Service
@Slf4j
public class GoogleGeocodingService {

    private final GeoApiContext geoApiContext;
    @Getter
    private final boolean apiEnabled;

    /**
     * Constructor with dependency injection.
     *
     * @param geoApiContext The GeoApiContext bean from configuration
     * @param apiEnabled Flag indicating if Google API is enabled
     */
    @Autowired
    public GoogleGeocodingService(GeoApiContext geoApiContext,
                                 @Value("${google.api.enabled}") boolean apiEnabled) {
        this.geoApiContext = geoApiContext;
        this.apiEnabled = apiEnabled;
    }

    /**
     * Geocode a query string to coordinates.
     *
     * @param query The query string to geocode
     * @return Coordinate object if successful, null otherwise
     */
    public Coordinate geocodeQuery(String query) {
        if (!apiEnabled || StringUtils.isBlank(query)) {
            return null;
        }

        try {
            // Call Google Geocoding API
            GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, query).await();

            if (results != null && results.length > 0 && results[0].geometry != null &&
                    results[0].geometry.location != null) {
                LatLng location = results[0].geometry.location;
                log.debug("Geocoded query to coordinates: {},{}", location.lat, location.lng);
                return new Coordinate(location.lat, location.lng);
            }
        } catch (Exception e) {
            log.error("Error geocoding query: {}", query, e);
        }

        return null;
    }

    /**
     * Get coordinates for a place ID.
     *
     * @param placeId The Google Place ID
     * @return Coordinate object if successful, null otherwise
     */
    public Coordinate getPlaceCoordinates(String placeId) {
        if (!apiEnabled || placeId == null || placeId.trim().isEmpty()) {
            return null;
        }

        try {
            // Get place details from Google API
            PlaceDetails details = PlacesApi.placeDetails(geoApiContext, placeId).await();

            if (details != null && details.geometry != null && details.geometry.location != null) {
                LatLng location = details.geometry.location;
                log.debug("Retrieved coordinates from place_id: {},{}", location.lat, location.lng);
                return new Coordinate(location.lat, location.lng);
            }
        } catch (Exception e) {
            log.error("Error getting coordinates for place ID: {}", placeId, e);
        }

        return null;
    }
}
