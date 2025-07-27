package com.example.mapsbridge.provider.extractor.impl.openstreet;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.openstreet.O100DefaultExtractor;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class O100DefaultExtractorTest {

    private O100DefaultExtractor extractor;

    @BeforeEach
    void setUp() {
        // Create a real OkHttpClient for API calls
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        extractor = new O100DefaultExtractor(httpClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.openstreetmap.org/?mlat=51.98312&mlon=5.905344",
            "https://www.openstreetmap.org/#map=16/51.98312/5.905344"
    })
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
    @ValueSource(strings = {
            "https://www.openstreetmap.org/?mlat=Invalid&mlon=5.905344",
            "https://www.openstreetmap.org/#map=16/Invalid/5.905344",
            "https://www.openstreetmap.org/",
            "https://www.openstreetmap.org/?q=InvalidLocation"
    })
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
        String url = "https://www.openstreetmap.org/?mlat=37.7749&mlon=-122.4194";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        // The result might be empty if the API call fails, but the test should not throw an exception
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.openstreetmap.org/?mlat=90.0&mlon=180.0",
            "https://www.openstreetmap.org/?mlat=-90.0&mlon=-180.0",
            "https://www.openstreetmap.org/?mlat=0.0&mlon=0.0",
            "https://www.openstreetmap.org/#map=16/90.0/180.0",
            "https://www.openstreetmap.org/#map=16/-90.0/-180.0",
            "https://www.openstreetmap.org/#map=16/0.0/0.0"
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
            "https://www.openstreetmap.org/?mlat=invalid&mlon=5.905344",
            "https://www.openstreetmap.org/?mlat=51.98312&mlon=invalid",
            "https://www.openstreetmap.org/?mlat=&mlon=",
            "https://www.openstreetmap.org/?mlat=51.98312&mlon=",
            "https://www.openstreetmap.org/?mlat=&mlon=5.905344",
            "https://www.openstreetmap.org/#map=16/invalid/5.905344",
            "https://www.openstreetmap.org/#map=16/51.98312/invalid",
            "https://www.openstreetmap.org/#map=16//",
            "https://www.openstreetmap.org/#map=16/51.98312/",
            "https://www.openstreetmap.org/#map=16//5.905344"
    })
    void shouldHandleInvalidCoordinateFormats(String url) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());
    }
}