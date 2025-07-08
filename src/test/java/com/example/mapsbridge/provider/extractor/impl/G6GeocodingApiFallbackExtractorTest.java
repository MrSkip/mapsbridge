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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class G6GeocodingApiFallbackExtractorTest {

    private G6GeocodingApiFallbackExtractor extractor;

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

        when(mockInputTypeCounterBuilder.tag(anyString(), anyString())).thenReturn(mockInputTypeCounterBuilder);
        when(mockInputTypeCounterBuilder.register(mockMeterRegistry)).thenReturn(mockCounter);

        extractor = new G6GeocodingApiFallbackExtractor(
                mockGeocodingService,
                mockUrlPatternExtractor,
                mockCounterBuilder,
                mockInputTypeCounterBuilder,
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
        when(mockUrlPatternExtractor.findCoordinates(url)).thenReturn(Optional.empty());
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
        verify(mockUrlPatternExtractor).findCoordinates(url);
        verify(mockUrlPatternExtractor).findPlaceId(url);
        verify(mockGeocodingService).getLocationFromPlaceId(placeId);
    }

    @Test
    void shouldExtractCoordinatesUsingAddressQuery() {
        // given
        String url = "https://www.google.com/maps?q=Dadawan+Arnhem,+Gele+Rijders+Plein+15,+6811+AV+Arnhem,+Netherlands";
        String query = "Dadawan Arnhem, Gele Rijders Plein 15, 6811 AV Arnhem, Netherlands";
        Coordinate expectedCoordinate = new Coordinate(40.7128, -74.0060);
        LocationResult expectedResult = LocationResult.fromCoordinatesAndName(expectedCoordinate, "Dadawan Arnhem");

        // Configure mocks
        when(mockUrlPatternExtractor.findCoordinates(url)).thenReturn(Optional.empty());
        when(mockUrlPatternExtractor.findPlaceId(url)).thenReturn(Optional.empty());
        when(mockUrlPatternExtractor.findAddressQuery(url)).thenReturn(Optional.of(query));
        when(mockGeocodingService.geocodeQuery(query)).thenReturn(expectedResult);

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        assertEquals(expectedCoordinate.getLat(), result.getCoordinates().getLat());
        assertEquals(expectedCoordinate.getLon(), result.getCoordinates().getLon());
        assertEquals("Dadawan Arnhem", result.getAddress());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findCoordinates(url);
        verify(mockUrlPatternExtractor).findPlaceId(url);
        verify(mockUrlPatternExtractor).findAddressQuery(url);
        verify(mockGeocodingService).geocodeQuery(query);
    }

    @Test
    void shouldReturnNullWhenNoLocationFound() {
        // given
        String url = "https://www.google.com/maps?invalid=true";

        // Configure mocks
        when(mockUrlPatternExtractor.findCoordinates(url)).thenReturn(Optional.empty());
        when(mockUrlPatternExtractor.findPlaceId(url)).thenReturn(Optional.empty());
        when(mockUrlPatternExtractor.findAddressQuery(url)).thenReturn(Optional.empty());

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNull(result.getCoordinates());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findCoordinates(url);
        verify(mockUrlPatternExtractor).findPlaceId(url);
        verify(mockUrlPatternExtractor).findAddressQuery(url);
    }

    @Test
    void shouldReturnNullForInvalidUrl() {
        // given
        String emptyUrl = "";
        String nullUrl = null;

        // when & then
        assertNull(extractor.extract(emptyUrl).getCoordinates());
        assertNull(extractor.extract(nullUrl).getCoordinates());
    }
}
