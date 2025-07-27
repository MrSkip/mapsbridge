package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class KomootMapProviderTest {

    private KomootMapProvider target;

    private static Stream<String> getValidUrls() {
        return Stream.of(
                "https://www.komoot.com/plan/@51.98312,5.905344",
                "https://komoot.com/plan/@51.98312,5.905344",
                "https://www.komoot.com/tour/123456789/@51.98312,5.905344",
                "https://www.komoot.com/smarttour/123456789/@51.98312,5.905344"
        ).distinct();
    }

    private static Stream<String> getInvalidUrls() {
        return Stream.of(
                "https://maps.apple.com/?q=San+Francisco",
                "https://example.com/somepath",
                "https://komoot.com/invalidpath"
        ).distinct();
    }

    @BeforeEach
    void setUp() {
        target = new KomootMapProvider(new OkHttpClient.Builder().build(), "https://www.komoot.com/plan/@{lat},{lon}", null);
    }

    @Test
    void testGetType() {
        assertEquals(MapType.KOMOOT, target.getType());
    }

    @ParameterizedTest
    @MethodSource("getInvalidUrls")
    void testExtractCoordinatesWithInvalidUrls(String url) {
        // when
        LocationResult locationResult = target.extractLocation(url);

        // then
        assertNull(locationResult.getCoordinates());
    }

    @ParameterizedTest
    @MethodSource("getValidUrls")
    void testIsProviderUrlWithValidUrls(String url) {
        // when
        boolean result = target.isProviderUrl(url);

        // then
        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({
            "https://maps.apple.com/?q=San+Francisco",
            "https://example.com/somepath",
            "https://komoot1.com/invalidpath"
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
