package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.service.GoogleGeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeocodingApiFallbackExtractorTest {

    private GeocodingApiFallbackExtractor extractor;

    @Mock
    private GoogleGeocodingService mockGeocodingService;

    @BeforeEach
    void setUp() {
        extractor = new GeocodingApiFallbackExtractor(mockGeocodingService);
    }

    @Test
    void shouldExtractPlaceIdFromUrlWithPlaceIdParameter() {
        // given
        // Test the first pattern: place_id= parameter
        String urlWithPlaceIdParameter = "https://www.google.com/maps/place/New+York+City,+NY,+USA/@40.7127753,-74.0059728,12z/data=!3m1!4b1!4m6!3m5!1s0x89c24fa5d33f083b:0xc80b8f06e177fe62!8m2!3d40.7127753!4d-74.0059728!16zL20vMDJfMjg2?entry=ttu&place_id=ChIJOwg_06VPwokRYv534QaPC8g";

        // when
        String placeId = extractor.findPlaceId(urlWithPlaceIdParameter);

        // then
        assertNotNull(placeId);
        assertEquals("ChIJOwg_06VPwokRYv534QaPC8g", placeId);
    }

    @Test
    void shouldExtractPlaceIdFromUrlWith1sPattern() {
        // given
        // Test the second pattern: !1s pattern
        String urlWith1sPattern = "https://www.google.com/maps/place/New+York+City/@40.7127753,-74.0059728,12z/data=!1sChIJOwg_06VPwokRYv534QaPC8g!2m1!3m1!1s0x89c24fa5d33f083b:0xc80b8f06e177fe62";

        // when
        String placeId = extractor.findPlaceId(urlWith1sPattern);

        // then
        assertNotNull(placeId);
        assertEquals("ChIJOwg_06VPwokRYv534QaPC8g", placeId);
    }

    @Test
    void shouldExtractPlaceIdFromUrlWith3m1sPattern() {
        // given
        // Test the third pattern: !3m\d+!1s
        String urlWith3m1sPattern = "https://www.google.com/maps/place/Statue+of+Liberty/data=!4m6!3m5!1s0x89c25090129c363d:0x40c6a5770d25022b!8m2!3d40.6892494!4d-74.0445004!16s%2Fm%2F072p8";

        // when
        String placeId = extractor.findPlaceId(urlWith3m1sPattern);

        // then
        assertNotNull(placeId);
        assertEquals("0x89c25090129c363d:0x40c6a5770d25022b", placeId);
    }

    @Test
    void shouldExtractCoordinatesUsingPlaceId() {
        // given
        when(mockGeocodingService.isApiEnabled()).thenReturn(true);
        String urlWithPlaceId = "https://www.google.com/maps/place/Statue+of+Liberty/data=!4m6!3m5!1s0x89c25090129c363d:0x40c6a5770d25022b";

        Coordinate expectedCoordinate = new Coordinate(40.6892494, -74.0445004);

        // Configure mock to return coordinates for the place ID
        when(mockGeocodingService.getPlaceCoordinates("0x89c25090129c363d:0x40c6a5770d25022b")).thenReturn(expectedCoordinate);

        // when
        Coordinate result = extractor.extract(urlWithPlaceId);

        // then
        assertNotNull(result);
        assertEquals(expectedCoordinate.getLat(), result.getLat());
        assertEquals(expectedCoordinate.getLon(), result.getLon());

        // Verify the service was called
        verify(mockGeocodingService).getPlaceCoordinates("0x89c25090129c363d:0x40c6a5770d25022b");
    }

    @Test
    void shouldExtractCoordinatesUsingAddressQuery() {
        // given
        when(mockGeocodingService.isApiEnabled()).thenReturn(true);

        String urlWithQuery = "https://www.google.com/maps?q=New+York";
        Coordinate expectedCoordinate = new Coordinate(40.7128, -74.0060);

        // Configure mock to return coordinates for the query
        when(mockGeocodingService.geocodeQuery("New York")).thenReturn(expectedCoordinate);

        // when
        Coordinate result = extractor.extract(urlWithQuery);

        // then
        assertNotNull(result);
        assertEquals(expectedCoordinate.getLat(), result.getLat());
        assertEquals(expectedCoordinate.getLon(), result.getLon());

        // Verify the service was called
        verify(mockGeocodingService).geocodeQuery("New York");
    }

    @Test
    void shouldReturnNullWhenApiDisabled() {
        // given
        when(mockGeocodingService.isApiEnabled()).thenReturn(false);
        String url = "https://www.google.com/maps?q=New+York";

        // when
        Coordinate result = extractor.extract(url);

        // then
        assertNull(result);
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
}
