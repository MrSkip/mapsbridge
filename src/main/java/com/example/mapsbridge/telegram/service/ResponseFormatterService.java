package com.example.mapsbridge.telegram.service;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.ConvertResponse;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.service.impl.MapConverterServiceImpl;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class ResponseFormatterService {

    private final MapConverterServiceImpl mapConverterService;

    private final Mustache mustacheTemplate;

    public ResponseFormatterService(MapConverterServiceImpl mapConverterService) {
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
            return formatResponse(response);
        } catch (Exception e) {
            log.error("Error converting message", e);
            return "Sorry, I couldn't process your message ...";
        }
    }

    private String formatResponse(ConvertResponse response) {
        Map<String, Object> context = new HashMap<>();

        for (Map.Entry<MapType, String> entry : response.getLinks().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                String mapUrl = String.format("%sUrl", entry.getKey().name().toLowerCase());
                context.put(mapUrl, entry.getValue());
            }
        }

        if (context.isEmpty()) {
            return "No valid map links found in your message.";
        }

        if (StringUtils.isNoneBlank(response.getName())) {
            context.put("name", response.getName());
        }

        if (StringUtils.isNoneBlank(response.getAddress())) {
            context.put("address", response.getAddress());
        }

        StringWriter writer = new StringWriter();
        mustacheTemplate.execute(writer, context);
        return writer.toString();
    }
}
