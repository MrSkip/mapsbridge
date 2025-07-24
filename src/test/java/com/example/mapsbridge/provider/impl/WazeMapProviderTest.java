package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.Coordinate;
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
class WazeMapProviderTest {

    private WazeMapProvider target;

    private static Stream<String> getValidUrls() {
        return Stream.of(
                "https://ul.waze.com/ul?ll=51.98312%2C5.905344&navigate=yes",
                "https://waze.com/ul?ll=51.98312,5.905344&navigate=yes",
                "https://www.waze.com/live-map/directions?to=ll.51.98312%2C5.905344",
                "https://www.waze.com/live-map/directions?to=ll.51.98312,5.905344",
                "https://waze.com/ul/hu1hpwrhps"
        ).distinct();
    }

    private static Stream<String> getInvalidUrls() {
        return Stream.of(
                "https://maps.apple.com/?q=San+Francisco"
//                "https://example.com/somepath",
//                "https://waze.com/invalidpath"
        ).distinct();
    }

    @BeforeEach
    void setUp() {
        target = new WazeMapProvider(new OkHttpClient.Builder().build(), "https://waze.com/ul?ll={lat},{lon}&navigate=yes");
    }

    @Test
    void testGetType() {
        assertEquals(MapType.WAZE, target.getType());
    }

    @ParameterizedTest
    @MethodSource("getValidUrls")
    void testExtractCoordinatesWithValidUrls(String url) {
        // when
        LocationResult locationResult = target.extractLocation(url);

        // then
        assertNotNull(locationResult);
        assertNotNull(locationResult.getCoordinates());
        assertEquals(51.98312, locationResult.getCoordinates().getLat());
        assertEquals(5.905344, locationResult.getCoordinates().getLon());
    }

    @ParameterizedTest
    @MethodSource("getInvalidUrls")
    void testExtractCoordinatesWithInvalidUrls(String url) {
        // when
        Coordinate coordinate = target.extractLocation(url).getCoordinates();

        // then
        assertNull(coordinate);
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
            "https://waze1.com/invalidpath"
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
