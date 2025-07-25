package com.example.mapsbridge.provider.extractor.waze;

import com.example.mapsbridge.provider.extractor.DefaultExtractor;
import okhttp3.OkHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Order(100)
@Component
public class W100DefaultExtractor extends DefaultExtractor implements WazeCoordinateExtractor {
    // Handle both standard "ll=" format and live-map "ll." format, plus URL-encoded commas (%2C)
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("ll[=.](?<lat>-?\\d+\\.?\\d*)(?:[,]|%2C)(?<lon>-?\\d+\\.?\\d*)");

    public W100DefaultExtractor(OkHttpClient httpClient) {
        super(httpClient, COORDINATE_PATTERN);
    }
}