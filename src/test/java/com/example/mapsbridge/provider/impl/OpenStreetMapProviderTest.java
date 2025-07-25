package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
class OpenStreetMapProviderTest {

    private OpenStreetMapProvider target;

    @BeforeEach
    void setUp() {
        target = new OpenStreetMapProvider(
                new OkHttpClient.Builder().build(),
                "https://www.openstreetmap.org/?mlat={lat}&mlon={lon}#map=16/{lat}/{lon}", List.of()
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
        String url = target.generateUrl(coordinate);

        // then
        assertEquals("https://www.openstreetmap.org/?mlat=51.98312&mlon=5.905344#map=16/51.98312/5.905344", url);
    }
}
