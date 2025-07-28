package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.google.G2LatLon3d4dExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class G2LatLon3D4DExtractorTest {

    private G2LatLon3d4dExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new G2LatLon3d4dExtractor();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://www.google.com/maps/place/St.+Walburga+Kapelle/@48.0698426,10.8694184,13z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d48.1001328!4d10.8898249!16s # 48.1001328 # 10.8898249",
            "https://www.google.com/maps/place/New+York/@40.7127753,-74.0059728,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7127753!4d-74.0059728!16s # 40.7127753 # -74.0059728",
            "https://www.google.com/maps/place/Central+Park/@40.7829,-73.9654,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7829!4d-73.9654!16s # 40.7829 # -73.9654",
            "https://www.google.com/maps/place/London/@51.5074,-0.1278,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d51.5074!4d-0.1278!16s # 51.5074 # -0.1278",
            "https://www.google.com/maps/place/Sydney/@-33.8688,151.2093,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d-33.8688!4d151.2093!16s # -33.8688 # 151.2093",
            "https://www.google.com/maps/place/Test/@0.0,0.0,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d0.0!4d0.0!16s # 0.0 # 0.0",
            "https://www.google.com/maps/place/Test/@90.0,180.0,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d90.0!4d180.0!16s # 90.0 # 180.0",
            "https://www.google.com/maps/place/Test/@-90.0,-180.0,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d-90.0!4d-180.0!16s # -90.0 # -180.0",
            "https://www.google.com/maps/place/Test/@12.123456,34.987654,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d12.123456!4d34.987654!16s # 12.123456 # 34.987654",
            "https://www.google.com/maps/place/Test/@1,-1,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d1!4d-1!16s # 1.0 # -1.0"
    }, delimiter = '#')
    void shouldExtractValidCoordinatesFromUrl(String url, double expectedLat, double expectedLon) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(expectedLat, coordinate.getLat(), 0.000001);
        assertEquals(expectedLon, coordinate.getLon(), 0.000001);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://www.google.com/maps/place/New+York/@40.7127753,-74.0059728,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7127753!4d-74.0059728!16s # New York",
            "https://www.google.com/maps/place/Central+Park/@40.7829,-73.9654,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7829!4d-73.9654!16s # Central Park",
            "https://www.google.com/maps/place/Times+Square/@40.7580,-73.9855,16z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7580!4d-73.9855!16s # Times Square",
            "https://www.google.com/maps/place/Eiffel+Tower/@48.8584,2.2945,17z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d48.8584!4d2.2945!16s # Eiffel Tower",
            "https://www.google.com/maps/place/Sydney+Opera+House/@-33.8568,151.2153,17z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d-33.8568!4d151.2153!16s # Sydney Opera House",
            "https://www.google.com/maps/place/123+Main+Street/@40.7128,-74.0060,16z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7128!4d-74.0060!16s # 123 Main Street",
            "https://www.google.com/maps/place/McDonald's/@40.7128,-74.0060,16z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7128!4d-74.0060!16s # McDonald's",
            "https://www.google.com/maps/place/St.+Walburga+Kapelle/@48.0698426,10.8694184,13z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d48.1001328!4d10.8898249!16s # St. Walburga Kapelle"
    }, delimiter = '#')
    void shouldExtractPlaceNameFromUrl(String url, String expectedPlaceName) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        assertEquals(expectedPlaceName, result.getPlaceName());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://www.google.com/maps/place/Test/@40.7128,invalid,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7128!4dinvalid!16s",
            "https://www.google.com/maps/place/Test/@invalid,-74.0060,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3dinvalid!4d-74.0060!16s",
            "https://www.google.com/maps/place/Test/@invalid,coordinates,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3dinvalid!4dcoordinates!16s",
            "https://www.google.com/maps/place/Test/@40.7128,-74.0060,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d48.invalid!4dinvalid!16s",
            "https://www.google.com/maps/place/Test/@40.7128,-74.0060,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d!4d-74.0060!16s",
            "https://www.google.com/maps/place/Test/@40.7128,-74.0060,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7128!4d!16s"
    })
    void shouldReturnEmptyResultForInvalidCoordinateFormat(String url) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());
        assertNull(result.getCoordinates());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "https://www.google.com/maps?q=New+York",
            "https://www.google.com/maps/place/New+York",
            "https://www.google.com/maps/search/New+York",
            "https://example.com/not-a-google-maps-url",
            "https://www.google.com/maps/place/Test/@40.7128,-74.0060,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7128!16s"
    })
    void shouldReturnEmptyResultForUrlsWithoutPattern(String url) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldReturnEmptyResultForNullUrl() {
        // when
        LocationResult result = extractor.extract(null);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldExtractLastCoordinatesWhenMultiplePatterns() {
        // given
        String url = "https://www.google.com/maps/place/Test/@40.7128,-74.0060,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d48.1001328!4d10.8898249!16s!3d51.5074!4d-0.1278";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(51.5074, coordinate.getLat(), 0.000001);
        assertEquals(-0.1278, coordinate.getLon(), 0.000001);
    }

    @Test
    void shouldHandleUrlWithoutPlaceNameButWithCoordinates() {
        // given
        String url = "https://www.google.com/maps/@40.7128,-74.0060,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7128!4d-74.0060!16s";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(40.7128, coordinate.getLat(), 0.000001);
        assertEquals(-74.0060, coordinate.getLon(), 0.000001);
        assertNull(result.getPlaceName());
    }

    @Test
    void shouldHandlePartiallyValidCoordinatesInMixedPattern() {
        // given - URL with one valid and one invalid coordinate pattern
        String url = "https://www.google.com/maps/place/Test/@40.7128,-74.0060,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3dinvalid!4dinvalid!16s!3d48.1001328!4d10.8898249";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(48.1001328, coordinate.getLat(), 0.000001);
        assertEquals(10.8898249, coordinate.getLon(), 0.000001);
    }

    @Test
    void shouldHandleVeryLongDecimalPrecision() {
        // given
        String url = "https://www.google.com/maps/place/Test/@40.7128123456789,-74.0060987654321,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d40.7128123456789!4d-74.0060987654321!16s";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(40.7128123456789, coordinate.getLat(), 0.0000000000001);
        assertEquals(-74.0060987654321, coordinate.getLon(), 0.0000000000001);
    }
}