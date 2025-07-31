package com.example.mapsbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for the map link conversion API.
 * The input can be either a map URL or coordinates in the format "lat,lon".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertRequest {
    
    /**
     * The input to convert, which can be:
     * - A map URL (e.g., "https://maps.google.com/?q=Statue+of+Liberty")
     * - Coordinates (e.g., "40.6892,-74.0445")
     */
    @NotBlank(message = "Input cannot be empty")
    private String input;
}