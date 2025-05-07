package com.example.mapsbridge.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mapsbridge.model.ConvertRequest;
import com.example.mapsbridge.model.ConvertResponse;
import com.example.mapsbridge.service.MapConverterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for the map link conversion API.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Map Converter", description = "API for converting map links between different providers")
public class MapConverterController {

    private final MapConverterService mapConverterService;

    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     * 
     * @param request The conversion request
     * @return The conversion response with coordinates and links
     */
    @PostMapping("/convert")
    @Operation(
        summary = "Convert map URL or coordinates",
        description = "Converts a map URL or coordinates to links for all supported map providers",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful conversion",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ConvertResponse.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "coordinates": {
                            "lat": 40.6892,
                            "lon": -74.0445
                          },
                          "links": {
                            "google": "https://www.google.com/maps?q=40.6892,-74.0445",
                            "apple": "https://maps.apple.com/?ll=40.6892,-74.0445",
                            "osm": "https://www.openstreetmap.org/?mlat=40.6892&mlon=-74.0445",
                            "bing": "https://www.bing.com/maps?q=40.6892,-74.0445",
                            "waze": "https://waze.com/ul?ll=40.6892,-74.0445&navigate=yes"
                          }
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                          "error": "Could not extract coordinates from URL: https://example.com"
                        }
                        """
                    )
                )
            )
        }
    )
    public ConvertResponse convert(@Valid @RequestBody ConvertRequest request) {
        log.info("Converting input: {}", request.getInput());
        return mapConverterService.convert(request);
    }
}
