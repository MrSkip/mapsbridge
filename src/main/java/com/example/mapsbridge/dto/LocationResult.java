package com.example.mapsbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a location result with coordinates and location name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResult {
    /**
     * The geographic coordinates of the location.
     */
    private Coordinate coordinates;

    /**
     * The name of the location.
     */
    private String address;

    private String placeName;

    /**
     * Creates a LocationResult with only coordinates.
     *
     * @param coordinates The coordinates of the location
     * @return LocationResult with the specified coordinates and null location name
     */
    public static LocationResult fromCoordinates(Coordinate coordinates) {
        return new LocationResult(coordinates, null, null);
    }

    /**
     * Creates a LocationResult with coordinates and location name.
     *
     * @param coordinates  The coordinates of the location
     * @param locationName The name of the location
     * @return LocationResult with the specified coordinates and location name
     */
    public static LocationResult fromCoordinatesAndName(Coordinate coordinates, String locationName) {
        return new LocationResult(coordinates, locationName, null);
    }

    /**
     * Checks if the location result has valid coordinates.
     *
     * @return true if coordinates are valid, false otherwise
     */
    public boolean hasValidCoordinates() {
        return coordinates != null && coordinates.isValid();
    }

    /**
     * Checks if the location result has a location name.
     *
     * @return true if location name is not null or empty, false otherwise
     */
    public boolean hasLocationName() {
        return address != null && !address.trim().isEmpty();
    }

    /**
     * Checks if this is a complete location result with both valid coordinates and a location name.
     *
     * @return true if both coordinates and location name are valid, false otherwise
     */
    public boolean isComplete() {
        return hasValidCoordinates() && hasLocationName();
    }
}
