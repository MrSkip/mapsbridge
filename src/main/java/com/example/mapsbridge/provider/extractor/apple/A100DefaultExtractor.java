package com.example.mapsbridge.provider.extractor.apple;

import com.example.mapsbridge.provider.extractor.DefaultExtractor;
import okhttp3.OkHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Order(100)
@Component
public class A100DefaultExtractor extends DefaultExtractor implements AppleCoordinateExtractor {
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("(?:ul\\?ll=|@?|&coordinate=)(?<lat>-?\\d+\\.\\d+),(?<lon>-?\\d+\\.\\d+)");

    public A100DefaultExtractor(OkHttpClient httpClient) {
        super(httpClient, COORDINATE_PATTERN);
    }
}
