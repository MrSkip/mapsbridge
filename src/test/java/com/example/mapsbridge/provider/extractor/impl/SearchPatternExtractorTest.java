package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.model.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchPatternExtractorTest {

    private SearchPatternExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new SearchPatternExtractor();
    }

    @Test
    void shouldExtractCoordinatesFromUrlWithSearchPattern() {
        // given
        // This URL contains coordinates in the format /search/LAT,LON
        String url = "https://www.google.com/maps/search/40.7128,-74.0060";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNotNull(coordinate);
        assertEquals(40.7128, coordinate.getLat());
        assertEquals(-74.0060, coordinate.getLon());
    }

    @Test
    void shouldReturnNullForUrlWithoutPattern() {
        // given
        String url = "https://www.google.com/maps/place/New+York";

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
        // This URL contains invalid coordinates in the /search/ pattern
        String url = "https://www.google.com/maps/search/invalid,coordinates";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNull(coordinate);
    }
}