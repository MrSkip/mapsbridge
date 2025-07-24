package com.example.mapsbridge.controller;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.ConvertResponse;
import com.example.mapsbridge.service.impl.MapConverterServiceImpl;
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
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MapConverterController {

    private final MapConverterServiceImpl mapConverterService;

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
