package com.example.mapsbridge.controller;

import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.config.metrics.MetricTags;
import com.example.mapsbridge.dto.request.ConvertRequest;
import com.example.mapsbridge.dto.response.WebConvertResponse;
import com.example.mapsbridge.service.converter.MapConverterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the web map link conversion API.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WebMapConverterController {

    private final MapConverterService<WebConvertResponse> mapConverterService;

    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     *
     * @param request The conversion request
     * @return The conversion response with coordinates and links
     */
    @PostMapping("/web/location/convert")
    public WebConvertResponse convert(@Valid @RequestBody ConvertRequest request) {
        LoggingContext.setEndpointType(MetricTags.WEB.toLowerCase());
        log.info("Converting web input: {}", request.getInput());
        return mapConverterService.convert(request);
    }

}
