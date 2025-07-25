package com.example.mapsbridge.provider.extractor.bing;

import com.example.mapsbridge.provider.extractor.DefaultExtractor;
import okhttp3.OkHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Order(100)
@Component
public class B100DefaultExtractor extends DefaultExtractor implements BingCoordinateExtractor {
    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            "q=(?<lat>-?\\d{1,3}[.,]\\d+)[,](?<lon>-?\\d{1,3}[.,]\\d+)" +
                    "|cp=(?<lat2>-?\\d{1,3}\\.\\d+)~(?<lon2>-?\\d{1,3}\\.\\d+)"
    );

    public B100DefaultExtractor(OkHttpClient httpClient) {
        super(httpClient, COORDINATE_PATTERN);
    }
}