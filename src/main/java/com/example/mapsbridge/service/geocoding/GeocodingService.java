package com.example.mapsbridge.service.geocoding;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;

/**
 * Interface for geocoding services.
 * Provides methods to convert between coordinates and location names.
 */
public interface GeocodingService {
    /**
     * Reverse geocodes coordinates to get a location name.
     *
     * @param coordinate The coordinates to reverse geocode
     * @return LocationResult with coordinates and location name
     */
    LocationResult reverseGeocode(Coordinate coordinate);

    /**
     * Gets location information from a place ID.
     *
     * @param placeId The place ID to lookup
     * @return LocationResult with coordinates and location name
     */
    LocationResult getLocationFromPlaceId(String placeId);

    /**
     * Forward geocodes a query to get coordinates and location name.
     *
     * @param query The address or place query to geocode
     * @return LocationResult with coordinates and location name
     */
    LocationResult geocodeQuery(String query);

    /**
     * Checks if this geocoding service is enabled.
     *
     * @return true if the service is enabled, false otherwise
     */
    boolean isEnabled();
}
