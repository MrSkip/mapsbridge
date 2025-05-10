package com.example.mapsbridge.telegram.service;

import com.example.mapsbridge.model.ConvertRequest;
import com.example.mapsbridge.model.ConvertResponse;
import com.example.mapsbridge.model.MapType;
import com.example.mapsbridge.service.MapConverterService;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service for formatting responses with map links.
 */
@Slf4j
@Service
public class ResponseFormatterService {

    private final MapConverterService mapConverterService;

    private final Mustache mustacheTemplate;

    public ResponseFormatterService(MapConverterService mapConverterService) {
        this.mapConverterService = mapConverterService;
        mustacheTemplate = initMustacheTemplate();
    }

    private Mustache initMustacheTemplate() {
        MustacheFactory mf = new DefaultMustacheFactory();
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/templates/telegram-maps-response.mustache"))
        )) {
            return mf.compile(reader, "mapLinksTemplate");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load mustache template", e);
        }
    }

    public String convertMessageToMapLinks(String message) {
        ConvertRequest request = new ConvertRequest(message.trim());

        try {
            ConvertResponse response = mapConverterService.convert(request);
            return formatResponse(response.getLinks());
        } catch (Exception e) {
            log.error("Error converting message: {}", e.getMessage());
            return "Sorry, I couldn't process your message ...";
        }
    }

    private String formatResponse(Map<MapType, String> links) {
        Map<String, Object> context = new HashMap<>();
        for (Map.Entry<MapType, String> entry : links.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                String mapUrl = String.format("%sUrl", entry.getKey().name().toLowerCase());
                context.put(mapUrl, entry.getValue());
            }
        }

        if (context.isEmpty()) {
            return "No valid map links found in your message.";
        }

        StringWriter writer = new StringWriter();
        mustacheTemplate.execute(writer, context);
        return writer.toString();
    }
}
