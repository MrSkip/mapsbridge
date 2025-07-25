package com.example.mapsbridge.provider.extractor.openstreet;

import com.example.mapsbridge.provider.extractor.DefaultExtractor;
import okhttp3.OkHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Order(100)
@Component
public class O100DefaultExtractor extends DefaultExtractor implements OpenStreetMapCoordinateExtractor {
    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            "mlat=(?<lat>-?\\d+(?:\\.\\d+)?)&mlon=(?<lon>-?\\d+(?:\\.\\d+)?)" +
                    "|#map=\\d+/(?<lat2>-?\\d+(?:\\.\\d+)?)/(?<lon2>-?\\d+(?:\\.\\d+)?)"
    );

    public O100DefaultExtractor(OkHttpClient httpClient) {
        super(httpClient, COORDINATE_PATTERN);
    }
}