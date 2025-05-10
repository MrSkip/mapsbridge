package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.model.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LatLon3d4dExtractorTest {

    private LatLon3d4dExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new LatLon3d4dExtractor();
    }

    @Test
    void shouldExtractCoordinatesFromUrlWith3d4dPattern() {
        // given
        // This URL contains coordinates in the format !3d!4d
        String url = "https://www.google.com/maps/place/St.+Walburga+Kapelle/@48.0698426,10.8694184,13z/data=!4m15!1m8!3m7!1s0x479c23a8dd6e0211:0xa721cdb237f131d6!2sKaufering,+Germany!3b1!8m2!3d48.088056!4d10.8567673!16s%2Fm%2F02q4cwf!3m5!1s0x479c23f7333d7805:0xf3eb5f42c3a31b9d!8m2!3d48.1001328!4d10.8898249!16s%2Fg%2F11f9yfn_x4?entry=ttu&g_ep=EgoyMDI1MDUwNS4wIKXMDSoASAFQAw%3D%3D";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNotNull(coordinate);
        assertEquals(48.1001328, coordinate.getLat());
        assertEquals(10.8898249, coordinate.getLon());
    }

    @Test
    void shouldExtractLastCoordinatesWhenMultiplePatterns() {
        // given
        // This URL contains multiple !3d!4d patterns, should extract the last one
        String url = "https://www.google.com/maps/place/New+York/@40.7127753,-74.0059728,12z/data=!3m1!4b1!4m6!3m5!1s0x89c24fa5d33f083b:0xc80b8f06e177fe62!8m2!3d40.7127753!4d-74.0059728!16zL20vMDJfMjg2";

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
        // This URL contains invalid coordinates in the !3d!4d pattern
        String url = "https://www.google.com/maps/place/Test/@invalid,coordinates,12z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d48.invalid!4dinvalid!16s";

        // when
        Coordinate coordinate = extractor.extract(url);

        // then
        assertNull(coordinate);
    }
}