package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.impl.url.UrlPatternExtractor;
import com.example.mapsbridge.service.geocoding.HybridGeocodingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class G6PlaceIdExtractorTest {

    private G6PlaceIdExtractor extractor;

    @Mock
    private HybridGeocodingService mockGeocodingService;

    @Mock
    private UrlPatternExtractor mockUrlPatternExtractor;

    @Mock
    private Counter.Builder mockCounterBuilder;

    @Mock
    private Counter.Builder mockInputTypeCounterBuilder;

    @Mock
    private MeterRegistry mockMeterRegistry;

    @Mock
    private Counter mockCounter;

    @BeforeEach
    void setUp() {
        // Configure the mock Counter.Builder to return a mock Counter when register() is called
        when(mockCounterBuilder.tag(anyString(), anyString())).thenReturn(mockCounterBuilder);
        when(mockCounterBuilder.register(mockMeterRegistry)).thenReturn(mockCounter);

        when(mockInputTypeCounterBuilder.register(mockMeterRegistry)).thenReturn(mockCounter);

        extractor = new G6PlaceIdExtractor(
                mockGeocodingService,
                mockUrlPatternExtractor,
                mockCounterBuilder,
                mockMeterRegistry);
    }

    @Test
    void shouldExtractCoordinatesUsingPlaceId() {
        // given
        String url = "https://www.google.com/maps/place/Statue+of+Liberty/data=!4m6!3m5!1s0x89c25090129c363d:0x40c6a5770d25022b";
        String placeId = "0x89c25090129c363d:0x40c6a5770d25022b";
        Coordinate expectedCoordinate = new Coordinate(40.6892494, -74.0445004);
        LocationResult expectedResult = LocationResult.fromCoordinatesAndName(expectedCoordinate, "Statue of Liberty");

        // Configure mocks
        when(mockUrlPatternExtractor.findPlaceId(url)).thenReturn(Optional.of(placeId));
        when(mockGeocodingService.getLocationFromPlaceId(placeId)).thenReturn(expectedResult);

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        assertEquals(expectedCoordinate.getLat(), result.getCoordinates().getLat());
        assertEquals(expectedCoordinate.getLon(), result.getCoordinates().getLon());
        assertEquals("Statue of Liberty", result.getAddress());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findPlaceId(url);
        verify(mockGeocodingService).getLocationFromPlaceId(placeId);
        verify(mockCounter).increment();
    }

    @Test
    void shouldReturnEmptyResultWhenNoPlaceIdFound() {
        // given
        String url = "https://www.google.com/maps?invalid=true";

        // Configure mocks
        when(mockUrlPatternExtractor.findPlaceId(url)).thenReturn(Optional.empty());

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findPlaceId(url);
        verify(mockGeocodingService, never()).getLocationFromPlaceId(anyString());
    }

    @Test
    void shouldReturnEmptyResultWhenPlaceIdResolutionFails() {
        // given
        String url = "https://www.google.com/maps/place/id=invalid_place_id";
        String placeId = "invalid_place_id";

        // Configure mocks
        when(mockUrlPatternExtractor.findPlaceId(url)).thenReturn(Optional.of(placeId));
        when(mockGeocodingService.getLocationFromPlaceId(placeId)).thenReturn(null);

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findPlaceId(url);
        verify(mockGeocodingService).getLocationFromPlaceId(placeId);
        verify(mockCounter, never()).increment();
    }

    @Test
    void shouldReturnEmptyResultForInvalidUrl() {
        // given
        String emptyUrl = "";
        String nullUrl = null;

        // when & then
        LocationResult emptyResult = extractor.extract(emptyUrl);
        LocationResult nullResult = extractor.extract(nullUrl);

        assertNotNull(emptyResult);
        assertNotNull(nullResult);
        assertFalse(emptyResult.hasValidCoordinates());
        assertFalse(nullResult.hasValidCoordinates());

        verify(mockUrlPatternExtractor, never()).findPlaceId(any());
    }
}