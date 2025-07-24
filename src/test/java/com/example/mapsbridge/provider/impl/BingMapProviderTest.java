package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BingMapProviderTest {

    private BingMapProvider target;

    @BeforeEach
    void setUp() {
        target = new BingMapProvider(new OkHttpClient.Builder().build(), "https://www.bing.com/maps?q={lat},{lon}");
    }

    @Test
    void testGetType() {
        assertEquals(MapType.BING, target.getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.bing.com/maps?q=51.98312,5.905344",
            "https://www.bing.com/maps?cp=51.98312~5.905344",
            "https://www.bing.com/maps?q=51,98312,5,905344"
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
            "https://www.bing.com/maps?q=InvalidLocation",
            "https://www.bing.com/maps?cp=Invalid~Location",
            "https://www.bing.com/maps?q=",
            "https://www.bing.com/maps"
    })
    void testExtractCoordinatesWithInvalidUrls(String url) {
        // when
        LocationResult locationResult = target.extractLocation(url);

        // then
        assertNull(locationResult.getCoordinates());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.bing.com/maps?q=51.98312,5.905344",
            "https://www.bing.com/maps?cp=51.98312~5.905344",
            "https://www.bing.com/maps?q=51,983180,5,902296"
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
            "https://www.bing.com/otherpath"
    })
    void testIsProviderUrlWithInvalidUrls(String url) {
        // when
        boolean result = target.isProviderUrl(url);

        // then
        assertFalse(result);
    }

    @Test
    void testIsProviderUrlWithNullUrl() {
        // when
        boolean result = target.isProviderUrl(null);

        // then
        assertFalse(result);
    }

    @Test
    void testIsProviderUrlWithEmptyUrl() {
        // when
        boolean result = target.isProviderUrl("");

        // then
        assertFalse(result);
    }
}
