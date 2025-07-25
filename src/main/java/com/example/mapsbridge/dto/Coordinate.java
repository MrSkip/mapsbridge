package com.example.mapsbridge.dto;

import com.example.mapsbridge.exception.InvalidCoordinateException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents geographic coordinates with latitude and longitude.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinate {
    private double lat;
    private double lon;

    /**
     * Creates a coordinate from a comma-separated string (e.g., "40.6892,-74.0445")
     * 
     * @param latLonString String in format "lat,lon"
     * @return Coordinate object
     * @throws InvalidCoordinateException if the string format is invalid
     */
    public static Coordinate fromString(String latLonString) {
        if (StringUtils.isBlank(latLonString)) {
            throw new InvalidCoordinateException("Coordinate string cannot be null or empty");
        }

        String[] parts;

        // First try splitting by comma
        if (latLonString.contains(",")) {
            parts = latLonString.split(",");
        } else {
            // If no comma, split by whitespace
            parts = latLonString.trim().split("\\s+");
        }

        if (parts.length != 2) {
            throw new InvalidCoordinateException("Coordinate string must be in format 'lat,lon' or 'lat lon'");
        }

        try {
            double lat = Double.parseDouble(parts[0].trim());
            double lon = Double.parseDouble(parts[1].trim());
            return new Coordinate(lat, lon);
        } catch (NumberFormatException e) {
            throw new InvalidCoordinateException("Invalid coordinate format: " + latLonString, e);
        }
    }

    /**
     * Checks if the coordinate is valid (within proper latitude/longitude ranges)
     * 
     * @return true if valid, false otherwise
     */
    @JsonIgnore
    public boolean isValid() {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    @Override
    public String toString() {
        return lat + "," + lon;
    }
}
