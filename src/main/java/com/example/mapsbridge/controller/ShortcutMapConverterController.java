package com.example.mapsbridge.controller;

import com.example.mapsbridge.dto.request.ConvertRequest;
import com.example.mapsbridge.dto.response.shortcut.ShortcutBaseResponse;
import com.example.mapsbridge.service.converter.MapConverterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the shortcut map link conversion API.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShortcutMapConverterController {

    private final MapConverterService<ShortcutBaseResponse> mapConverterService;

    /**
     * Convert a map URL or coordinates to links for all supported map providers.
     *
     * @param request The conversion request
     * @return The conversion response with coordinates and links
     */
    @PostMapping("/shortcut/location/convert")
    public ShortcutBaseResponse shortcutConvert(@RequestBody ConvertRequest request) {
        log.info("Converting input for shortcut: {}", request.getInput());
        return mapConverterService.convert(request);
    }
}