package com.example.mapsbridge.provider.extractor.impl.waze;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.waze.W100DefaultExtractor;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class W100DefaultExtractorTest {

    private W100DefaultExtractor extractor;

    private static Stream<String> getValidUrls() {
        return Stream.of(
                "https://ul.waze.com/ul?ll=51.98312%2C5.905344&navigate=yes",
                "https://waze.com/ul?ll=51.98312,5.905344&navigate=yes",
                "https://www.waze.com/live-map/directions?to=ll.51.98312%2C5.905344",
                "https://www.waze.com/live-map/directions?to=ll.51.98312,5.905344"
        ).distinct();
    }

    private static Stream<String> getInvalidUrls() {
        return Stream.of(
                "https://maps.apple.com/?q=San+Francisco",
                "https://example.com/somepath",
                "https://waze.com/invalidpath",
                "https://waze.com/ul/hu1hpwrhps" // This is a valid Waze URL but doesn't have coordinates in the URL
        ).distinct();
    }

    @BeforeEach
    void setUp() {
        // Create a real OkHttpClient for API calls
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        extractor = new W100DefaultExtractor(httpClient);
    }

    @ParameterizedTest
    @MethodSource("getValidUrls")
    void shouldExtractCoordinatesFromValidUrls(String url) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(51.98312, coordinate.getLat(), 0.0001);
        assertEquals(5.905344, coordinate.getLon(), 0.0001);
    }

    @ParameterizedTest
    @MethodSource("getInvalidUrls")
    void shouldReturnEmptyResultForInvalidUrls(String url) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());
    }

    @Test
    void shouldHandleNullUrl() {
        // when
        LocationResult result = extractor.extract(null);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());
    }

    @Test
    void shouldHandleEmptyUrl() {
        // when
        LocationResult result = extractor.extract("");

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());
    }

    @Test
    void shouldHandleRealApiCall() {
        // This test uses a real URL that would require an API call to resolve
        // The DefaultExtractor has logic to follow redirects and fetch content
        String url = "https://waze.com/ul?ll=37.7749,-122.4194&navigate=yes";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        // The result might be empty if the API call fails, but the test should not throw an exception
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://waze.com/ul?ll=90.0,180.0&navigate=yes",
            "https://waze.com/ul?ll=-90.0,-180.0&navigate=yes",
            "https://waze.com/ul?ll=0.0,0.0&navigate=yes",
            "https://www.waze.com/live-map/directions?to=ll.90.0,180.0",
            "https://www.waze.com/live-map/directions?to=ll.-90.0,-180.0",
            "https://www.waze.com/live-map/directions?to=ll.0.0,0.0"
    })
    void shouldHandleEdgeCaseCoordinates(String url) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        // The result might be empty if the pattern doesn't match, but the test should not throw an exception
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://waze.com/ul?ll=invalid,5.905344&navigate=yes",
            "https://waze.com/ul?ll=51.98312,invalid&navigate=yes",
            "https://waze.com/ul?ll=,&navigate=yes",
            "https://waze.com/ul?ll=51.98312,&navigate=yes",
            "https://waze.com/ul?ll=,5.905344&navigate=yes",
            "https://www.waze.com/live-map/directions?to=ll.invalid,5.905344",
            "https://www.waze.com/live-map/directions?to=ll.51.98312,invalid",
            "https://www.waze.com/live-map/directions?to=ll.,",
            "https://www.waze.com/live-map/directions?to=ll.51.98312,",
            "https://www.waze.com/live-map/directions?to=ll.,5.905344"
    })
    void shouldHandleInvalidCoordinateFormats(String url) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());
    }
}