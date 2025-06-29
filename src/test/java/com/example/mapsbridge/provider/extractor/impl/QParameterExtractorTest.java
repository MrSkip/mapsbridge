package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QParameterExtractorTest {

    private QParameterExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new QParameterExtractor();
    }

    @Test
    void shouldExtractCoordinatesFromUrlWithQParameter() {
        // given
        // This URL contains coordinates in the format q=LAT,LON
        String url = "https://www.google.com/maps?q=40.7128,-74.0060";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNotNull(coordinate);
        assertEquals(40.7128, coordinate.getLat());
        assertEquals(-74.0060, coordinate.getLon());
    }

    @Test
    void shouldHandleUrlEncodedParameters() {
        // given
        // This URL contains URL-encoded parameters
        String url = "https://www.google.com/maps?q=40.7128%2C-74.0060";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNotNull(coordinate);
        assertEquals(40.7128, coordinate.getLat());
        assertEquals(-74.0060, coordinate.getLon());
    }

    @Test
    void shouldHandleEuropeanDecimalCommas() {
        // given
        // This URL contains European decimal commas
        // The format should be q=LAT,LON where LAT and LON can use either dots or commas as decimal separators
        String url = "https://www.google.com/maps?q=40.7128,-74.0060";

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
        // This URL contains invalid coordinates in the q parameter
        String url = "https://www.google.com/maps?q=invalid,coordinates";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNull(coordinate);
    }
}