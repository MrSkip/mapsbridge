package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.metrics.MapProviderMetrics;
import com.example.mapsbridge.provider.AbstractMapProvider;
import com.example.mapsbridge.provider.extractor.google.GoogleCoordinateExtractor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
            MeterRegistry meterRegistry,
            MapProviderMetrics mapProviderMetrics) {
        super(httpClient, urlTemplate, URL_PATTERN, extractors, mapProviderMetrics);
        this.googleMapsExtractorCounterBuilder = googleMapsExtractorCounterBuilder;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public MapType getType() {
        return MapType.GOOGLE;
    }

    private void trackUsage(String extractorName) {
        // Track which extractor was used with metrics
        googleMapsExtractorCounterBuilder
                .tag("extractor", extractorName)
                .register(meterRegistry)
                .increment();
    }

}
