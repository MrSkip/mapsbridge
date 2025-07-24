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
class G7AddressGeocodingExtractorTest {

    private G7AddressGeocodingExtractor extractor;

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

        extractor = new G7AddressGeocodingExtractor(
                mockGeocodingService,
                mockUrlPatternExtractor,
                mockCounterBuilder,
                mockMeterRegistry);
    }

    @Test
    void shouldExtractCoordinatesUsingAddressQuery() {
        // given
        String url = "https://www.google.com/maps?q=Dadawan+Arnhem,+Gele+Rijders+Plein+15,+6811+AV+Arnhem,+Netherlands";
        String query = "Dadawan Arnhem, Gele Rijders Plein 15, 6811 AV Arnhem, Netherlands";
        Coordinate expectedCoordinate = new Coordinate(40.7128, -74.0060);
        LocationResult geocodingResult = LocationResult.fromCoordinatesAndName(expectedCoordinate, "Dadawan Arnhem");

        // Configure mocks
        when(mockUrlPatternExtractor.findAddressQuery(url)).thenReturn(Optional.of(query));
        when(mockGeocodingService.geocodeQuery(query)).thenReturn(geocodingResult);

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        assertEquals(expectedCoordinate.getLat(), result.getCoordinates().getLat());
        assertEquals(expectedCoordinate.getLon(), result.getCoordinates().getLon());
        assertEquals(query, result.getAddress());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findAddressQuery(url);
        verify(mockGeocodingService).geocodeQuery(query);
        verify(mockCounter).increment();
    }

    @Test
    void shouldReturnEmptyResultWhenNoAddressQueryFound() {
        // given
        String url = "https://www.google.com/maps?invalid=true";

        // Configure mocks
        when(mockUrlPatternExtractor.findAddressQuery(url)).thenReturn(Optional.empty());

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findAddressQuery(url);
        verify(mockGeocodingService, never()).geocodeQuery(anyString());
    }

    @Test
    void shouldReturnEmptyResultWhenGeocodingFails() {
        // given
        String url = "https://www.google.com/maps?q=invalid_address";
        String query = "invalid_address";

        // Configure mocks
        when(mockUrlPatternExtractor.findAddressQuery(url)).thenReturn(Optional.of(query));
        when(mockGeocodingService.geocodeQuery(query)).thenReturn(null);

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertFalse(result.hasValidCoordinates());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findAddressQuery(url);
        verify(mockGeocodingService).geocodeQuery(query);
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

        verify(mockUrlPatternExtractor, never()).findAddressQuery(any());
    }
}