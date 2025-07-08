package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.provider.extractor.GoogleCoordinateExtractor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Google Maps provider implementation.
 * Uses Chain of Responsibility pattern for coordinate extraction.
 */
@Service
@Slf4j
public class GoogleMapProvider extends AbstractMapProvider {


    private static final Pattern URL_PATTERN = Pattern.compile("https?://(www\\.)?google\\.com/maps.*|https?://maps\\.google\\.com.*|https?://maps\\.app\\.goo\\.gl/.*|https?://goo\\.gl/maps/.*");
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("q=(?<lat>-?\\d+\\.?\\d*),(?<lon>-?\\d+\\.?\\d*)");

    private static final List<String> SHORTENED_URL_PREFIXES = Arrays.asList(
            "https://maps.app.goo.gl/",
            "https://goo.gl/maps/"
    );

    private final List<GoogleCoordinateExtractor> extractors;
    private final Counter.Builder googleMapsExtractorCounterBuilder;
    private final MeterRegistry meterRegistry;

    /**
     * Constructor with dependency injection.
     *
     * @param httpClient  The OkHttpClient for HTTP requests
     * @param urlTemplate The URL template from configuration
     * @param extractors  The list of coordinate extractors
     */
    @Autowired
    public GoogleMapProvider(
            OkHttpClient httpClient,
            @Value("${maps.google.url:https://www.google.com/maps?q={lat},{lon}}") String urlTemplate,
            List<GoogleCoordinateExtractor> extractors,
            Counter.Builder googleMapsExtractorCounterBuilder,
            MeterRegistry meterRegistry) {
        super(httpClient, urlTemplate, URL_PATTERN, COORDINATE_PATTERN);
        this.extractors = extractors;
        this.googleMapsExtractorCounterBuilder = googleMapsExtractorCounterBuilder;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public MapType getType() {
        return MapType.GOOGLE;
    }

    @Override
    public LocationResult extractLocation(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        String finalUrl = getResolvedUrlOrDefault(url);

        // Apply each extractor in the chain until one returns a non-null result
        for (GoogleCoordinateExtractor extractor : extractors) {
            LocationResult locationResult = extractor.extract(finalUrl);
            if (locationResult.hasValidCoordinates()) {
                String extractorName = extractor.getClass().getSimpleName();
                Coordinate coordinate = locationResult.getCoordinates();
                log.info("Extracted coordinates using {}: {},{}", extractorName, coordinate.getLat(), coordinate.getLon());

                trackUsage(extractorName);

                return locationResult;
            }
        }

        return null;
    }

    private String getResolvedUrlOrDefault(String url) {
        for (String prefix : SHORTENED_URL_PREFIXES) {
            if (url.startsWith(prefix)) {
                log.debug("Resolving shortened URL: {}", url);
                return followRedirects(url);
            }
        }
        return url;
    }

    private void trackUsage(String extractorName) {
        // Track which extractor was used with metrics
        googleMapsExtractorCounterBuilder
                .tag("extractor", extractorName)
                .register(meterRegistry)
                .increment();
    }

}
