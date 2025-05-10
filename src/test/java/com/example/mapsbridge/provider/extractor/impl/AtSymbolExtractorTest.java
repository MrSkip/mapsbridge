package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.model.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtSymbolExtractorTest {

    private AtSymbolExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new AtSymbolExtractor();
    }

    @Test
    void shouldExtractCoordinatesFromUrlWithAtSymbolPattern() {
        // given
        // This URL contains coordinates in the format @LAT,LON
        String url = "https://www.google.com/maps/place/New+York/@40.7127753,-74.0059728,12z";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNotNull(coordinate);
        assertEquals(40.7127753, coordinate.getLat());
        assertEquals(-74.0059728, coordinate.getLon());
    }

    @Test
    void shouldReturnNullForUrlWithoutPattern() {
        // given
        String url = "https://www.google.com/maps?q=New+York";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNull(coordinate);
    }

    @Test
    void shouldReturnNullForInvalidUrl() {
        // given
        String emptyUrl = "";
        String nullUrl = null;

        // when & then
        assertNull(extractor.extract(emptyUrl));
        assertNull(extractor.extract(nullUrl));
    }

    @Test
    void shouldHandleInvalidCoordinateFormat() {
        // given
        // This URL contains invalid coordinates in the @ pattern
        String url = "https://www.google.com/maps/place/Test/@invalid,coordinates,12z";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNull(coordinate);
    }
}