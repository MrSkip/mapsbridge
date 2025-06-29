package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.Coordinate;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AppleMapProviderTest {

    private AppleMapProvider target;

    @BeforeEach
    void setUp() {
        target = new AppleMapProvider(new OkHttpClient.Builder().build(), "https://maps.apple.com/?q={lat},{lon}");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://maps.apple.com/?ul?ll=51.98312,5.905344",
            "https://maps.apple.com/?@51.98312,5.905344",
            "https://maps.apple.com/?&coordinate=51.98312,5.905344",
    })
    void testExtractCoordinatesWithValidUrls(String url) {
        // when
        Coordinate coordinate = target.extractCoordinates(url);

        // then
        assertEquals(51.98312, coordinate.getLat());
        assertEquals(5.905344, coordinate.getLon());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://maps.apple.com/?q=San+Francisco",
            "https://maps.apple.com/?invalidFormat",
            "https://maps.apple.com/?q=Invalid+Location"
    })
    void testExtractCoordinatesWithInvalidUrls(String url) {
        // when
        Coordinate coordinate = target.extractCoordinates(url);

        // then
        assertNull(coordinate);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://maps.apple.com/?ul?ll=51.98312,5.905344",
            "https://maps.apple.com/?@51.98312,5.905344",
            "https://maps.apple.com/?&coordinate=51.98312,5.905344"
    })
    void testIsProviderUrlWithValidUrls(String url) {
        // when
        boolean result = target.isProviderUrl(url);

        // then
        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "htt://maps.apple.com",
            "https://bing.com/maps",
            "https://www.google.com/maps/embed?pb=example",
            "https://www.example.com/maps",
            "https://www.google.com/otherpath",
            "https://maps.apple.com1",
            "https://1maps.apple.com",
            "http://maps.apple.com",
            "https://maps.app.goo.gl/GABXoJ8BsncR9TnMA",
            "https://maps.app.goo.gl/MhW3Jh8q1hrb8rkf7?g_st=com.google.maps.preview.copy",
            "https://www.google.com/maps/place/Berlin",
            "https://www.google.de/maps/dir/Start/End",
            "https://www.google.com/maps/search/?api=1&query=Berlin",
            "https://goo.gl/maps/abcd1234",
            "https://maps.app.goo.gl/abcd1234"
    })
    void testIsNotSupported(String testUrl) {
        assertFalse(target.isProviderUrl(testUrl), testUrl);
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
