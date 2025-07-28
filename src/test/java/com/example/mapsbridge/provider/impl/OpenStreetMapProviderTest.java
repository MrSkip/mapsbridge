package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.config.metrics.tracker.MapProviderTracker;
import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(MockitoExtension.class)
class OpenStreetMapProviderTest {

    @Mock
    private MapProviderTracker mockMetrics;

    private OpenStreetMapProvider target;

    @BeforeEach
    void setUp() {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        target = new OpenStreetMapProvider(
                httpClient,
                "https://www.openstreetmap.org/?mlat={lat}&mlon={lon}#map=16/{lat}/{lon}",
                List.of(new com.example.mapsbridge.provider.extractor.openstreet.O100DefaultExtractor(httpClient)),
                mockMetrics
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.openstreetmap.org/?mlat=51.98312&mlon=5.905344",
            "https://www.openstreetmap.org/#map=16/51.98312/5.905344"
    })
    void testExtractCoordinatesWithValidUrls(String url) {
        // when
        LocationResult locationResult = target.extractLocation(url);

        // then
        assertNotNull(locationResult);
        assertNotNull(locationResult.getCoordinates());
        assertEquals(51.98312, locationResult.getCoordinates().getLat(), 0.0001);
        assertEquals(5.905344, locationResult.getCoordinates().getLon(), 0.0001);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.openstreetmap.org/?mlat=Invalid&mlon=5.905344",
            "https://www.openstreetmap.org/#map=16/Invalid/5.905344",
            "https://www.openstreetmap.org/",
            "https://www.openstreetmap.org/?q=InvalidLocation"
    })
    void testExtractCoordinatesWithInvalidUrls(String url) {
        // when
        LocationResult locationResult = target.extractLocation(url);

        // then
        assertNull(locationResult.getCoordinates());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.openstreetmap.org/?mlat=51.98312&mlon=5.905344",
            "https://www.openstreetmap.org/#map=16/51.98312/5.905344"
    })
    void testIsProviderUrlWithValidUrls(String url) {
        // when
        boolean result = target.isProviderUrl(url);

        // then
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://maps.google.com/maps?q=51.98312,5.905344",
            "https://example.com/maps",
            "https://www.bing.com/maps"
    })
    void testIsProviderUrlWithInvalidUrls(String url) {
        // when
        boolean result = target.isProviderUrl(url);

        // then
        assertFalse(result);
    }

    @Test
    void testGenerateUrl() {
        // given
        Coordinate coordinate = new Coordinate(51.98312, 5.905344);

        // when
        String url = target.generateUrl(LocationResult.fromCoordinates(coordinate));

        // then
        assertEquals("https://www.openstreetmap.org/?mlat=51.98312&mlon=5.905344#map=16/51.98312/5.905344", url);
    }
}
