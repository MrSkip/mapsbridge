package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.config.metrics.tracker.MapProviderTracker;
import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.apple.A100DefaultExtractor;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AppleMapProviderTest {

    @Mock
    private MapProviderTracker mockMetrics;

    private AppleMapProvider target;

    @BeforeEach
    void setUp() {
        target = new AppleMapProvider(new OkHttpClient.Builder().build(), "https://maps.apple.com/?q={lat},{lon}", List.of(new A100DefaultExtractor(new OkHttpClient.Builder().build())), mockMetrics);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://maps.apple.com/?q=San+Francisco",
            "https://maps.apple.com/?invalidFormat",
            "https://maps.apple.com/?q=Invalid+Location"
    })
    void testExtractCoordinatesWithInvalidUrls(String url) {
        // when
        LocationResult locationResult = target.extractLocation(url);

        // then
        assertNull(locationResult.getCoordinates());
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

    @Test
    void testGenerateUrlWithAddressAndPlaceName() {
        // given
        Coordinate coordinate = new Coordinate(48.0973715, 10.8743395);
        LocationResult location = new LocationResult(null, coordinate, "Am Hohlweg 1, 86916 Kaufering, Germany", "Stephan Mayrock");

        // when
        String url = target.generateUrl(location);

        // then
        assertTrue(url.startsWith("https://maps.apple.com/place?"));
        assertTrue(url.contains("ll=48.0973715,10.8743395"));
        assertTrue(url.contains("address=Am+Hohlweg+1%2C+86916+Kaufering%2C+Germany"));
        assertTrue(url.contains("q=Stephan+Mayrock"));
    }

    @Test
    void testGenerateUrlWithAddressOnly() {
        // given
        Coordinate coordinate = new Coordinate(48.0973715, 10.8743395);
        LocationResult location = new LocationResult(null, coordinate, "Am Hohlweg 1, 86916 Kaufering, Germany", null);

        // when
        String url = target.generateUrl(location);

        // then
        assertTrue(url.startsWith("https://maps.apple.com/place?"));
        assertTrue(url.contains("ll=48.0973715,10.8743395"));
        assertTrue(url.contains("address=Am+Hohlweg+1%2C+86916+Kaufering%2C+Germany"));
        assertFalse(url.contains("q="));
    }

    @Test
    void testGenerateUrlWithCoordinatesOnly() {
        // given
        Coordinate coordinate = new Coordinate(48.0973715, 10.8743395);
        LocationResult location = new LocationResult(null, coordinate, null, null);

        // when
        String url = target.generateUrl(location);

        // then
        assertEquals("https://maps.apple.com/?q=48.0973715,10.8743395", url);
        assertFalse(url.contains("address="));
    }
}
