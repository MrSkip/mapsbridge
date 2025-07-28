package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.google.G4QParameterExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class G4QParameterExtractorTest {

    private G4QParameterExtractor extractor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configure mocks

        extractor = new G4QParameterExtractor();
    }

    @Test
    void shouldExtractCoordinatesFromStandardDecimalFormat() {
        // given
        String url = "https://www.google.com/maps?q=40.7128,-74.0060";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(40.7128, result.getCoordinates().getLat());
        assertEquals(-74.0060, result.getCoordinates().getLon());
    }

    @Test
    void shouldExtractCoordinatesFromEuropeanDecimalFormat() {
        // given
        String url = "https://www.google.com/maps?q=40,7128,-74,0060";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(40.7128, result.getCoordinates().getLat());
        assertEquals(-74.0060, result.getCoordinates().getLon());
    }

    @Test
    void shouldExtractCoordinatesFromMixedDecimalFormat() {
        // given
        String url = "https://www.google.com/maps?q=40.7128,-74,0060";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(40.7128, result.getCoordinates().getLat());
        assertEquals(-74.0060, result.getCoordinates().getLon());
    }

    @Test
    void shouldExtractNegativeCoordinates() {
        // given
        String url = "https://www.google.com/maps?q=-40.7128,-74.0060";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(-40.7128, result.getCoordinates().getLat());
        assertEquals(-74.0060, result.getCoordinates().getLon());
    }

    @Test
    void shouldExtractCoordinatesWithoutDecimals() {
        // given
        String url = "https://www.google.com/maps?q=40,-74";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(40.0, result.getCoordinates().getLat());
        assertEquals(-74.0, result.getCoordinates().getLon());
    }

    @Test
    void shouldHandleUrlDecodingCorrectly() {
        // given
        String encodedUrl = "https://www.google.com/maps?q=40.7128%2C-74.0060";

        // when
        LocationResult result = extractor.extract(encodedUrl);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(40.7128, result.getCoordinates().getLat());
        assertEquals(-74.0060, result.getCoordinates().getLon());
    }

    @Test
    void shouldReturnEmptyResultForNullUrl() {
        // given
        String nullUrl = null;

        // when
        LocationResult result = extractor.extract(nullUrl);

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldReturnEmptyResultForEmptyUrl() {
        // given
        String emptyUrl = "";

        // when
        LocationResult result = extractor.extract(emptyUrl);

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldReturnEmptyResultForBlankUrl() {
        // given
        String blankUrl = "   ";

        // when
        LocationResult result = extractor.extract(blankUrl);

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldReturnEmptyResultForUrlWithoutQParameter() {
        // given
        String url = "https://www.google.com/maps/place/New+York";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldReturnEmptyResultForUrlWithInvalidQParameter() {
        // given
        String url = "https://www.google.com/maps?q=New+York";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldHandleInvalidCoordinateFormat() {
        // given
        String url = "https://www.google.com/maps?q=invalid,coordinates";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldHandlePartiallyInvalidCoordinates() {
        // given
        String url = "https://www.google.com/maps?q=40.7128,invalid";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldHandleUrlWithMultipleParameters() {
        // given
        String url = "https://www.google.com/maps?q=40.7128,-74.0060&zoom=15&layer=traffic";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(40.7128, result.getCoordinates().getLat());
        assertEquals(-74.0060, result.getCoordinates().getLon());
    }

    @Test
    void shouldHandleUrlWithQParameterInMiddle() {
        // given
        String url = "https://www.google.com/maps?zoom=15&q=40.7128,-74.0060&layer=traffic";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(40.7128, result.getCoordinates().getLat());
        assertEquals(-74.0060, result.getCoordinates().getLon());
    }

    @Test
    void shouldHandleVeryLargeCoordinates() {
        // given
        String url = "https://www.google.com/maps?q=180.0000,-180.0000";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(180.0, result.getCoordinates().getLat());
        assertEquals(-180.0, result.getCoordinates().getLon());
    }

    @Test
    void shouldHandleHighPrecisionCoordinates() {
        // given
        String url = "https://www.google.com/maps?q=40.712823456,-74.006012345";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(40.712823456, result.getCoordinates().getLat());
        assertEquals(-74.006012345, result.getCoordinates().getLon());
    }

    @Test
    void shouldHandleMalformedUrlGracefully() {
        // given
        String malformedUrl = "not-a-valid-url";

        // when
        LocationResult result = extractor.extract(malformedUrl);

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }
}