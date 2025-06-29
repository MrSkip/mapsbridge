package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.Coordinate;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OpenStreetMapProviderTest {

    private OpenStreetMapProvider target;

    @BeforeEach
    void setUp() {
        target = new OpenStreetMapProvider(
                new OkHttpClient.Builder().build(),
                "https://www.openstreetmap.org/?mlat={lat}&mlon={lon}#map=16/{lat}/{lon}"
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.openstreetmap.org/?mlat=51.98312&mlon=5.905344",
            "https://www.openstreetmap.org/#map=16/51.98312/5.905344"
    })
    void testExtractCoordinatesWithValidUrls(String url) {
        // when
        Coordinate coordinate = target.extractCoordinates(url);

        // then
        assertNotNull(coordinate);
        assertEquals(51.98312, coordinate.getLat(), 0.0001);
        assertEquals(5.905344, coordinate.getLon(), 0.0001);
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
        Coordinate coordinate = target.extractCoordinates(url);

        // then
        assertNull(coordinate);
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