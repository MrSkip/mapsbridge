package com.example.mapsbridge.controller;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.ConvertResponse;
import com.example.mapsbridge.service.MapConverterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ConvertResponse convert(@Valid @RequestBody ConvertRequest request) {
        log.info("Converting input: {}", request.getInput());
        return mapConverterService.convert(request);
    }
}
