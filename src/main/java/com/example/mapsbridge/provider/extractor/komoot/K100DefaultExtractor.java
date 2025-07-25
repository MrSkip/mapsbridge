package com.example.mapsbridge.provider.extractor.komoot;

import com.example.mapsbridge.provider.extractor.DefaultExtractor;
import okhttp3.OkHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Order(100)
@Component
public class K100DefaultExtractor extends DefaultExtractor implements KomootCoordinateExtractor {
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("@(?<lat>-?\\d+\\.?\\d*),(?<lon>-?\\d+\\.?\\d*)");

    public K100DefaultExtractor(OkHttpClient httpClient) {
        super(httpClient, COORDINATE_PATTERN);
    }
}