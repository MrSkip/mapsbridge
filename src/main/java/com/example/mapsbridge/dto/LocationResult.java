package com.example.mapsbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a location result with coordinates and location name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResult {
    private MapType mapSource;
    private String originalUrl;

    /**
     * The geographic coordinates of the location.
     */
    private Coordinate coordinates;

    /**
     * The name of the location.
     */
    private String address;

    /**
     * The place name for the location.
     */
    private String placeName;

    /**
     * Creates a LocationResult with only coordinates.
     *
     * @param coordinates The coordinates of the location
     * @return LocationResult with the specified coordinates and null location name
     */
    public static LocationResult fromCoordinates(Coordinate coordinates) {
        return new LocationResult(null, null, coordinates, null, null);
    }

    /**
     * Creates a LocationResult with coordinates and location name.
     *
     * @param coordinates The coordinates of the location
     * @param address The address of the location
     * @return LocationResult with the specified coordinates and location name
     */
    public static LocationResult fromCoordinatesAndName(Coordinate coordinates, String address) {
        return new LocationResult(null, null, coordinates, address, null);
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
    public boolean hasValidAddress() {
        return StringUtils.isNoneBlank(address);
    }

    /**
     * Checks if the location result has a valid place name.
     *
     * @return true if place name is not null or empty, false otherwise
     */
    public boolean hasValidPlaceName() {
        return StringUtils.isNoneBlank(placeName);
    }

    /**
     * Returns a coordinate parameter string for URL building (e.g., "lat,lon").
     * Returns empty string if coordinates are invalid.
     *
     * @return The coordinate parameter string
     */
    public String getCoordinateParam() {
        if (!hasValidCoordinates()) {
            return "";
        }
        return coordinates.getLat() + "," + coordinates.getLon();
    }
}
