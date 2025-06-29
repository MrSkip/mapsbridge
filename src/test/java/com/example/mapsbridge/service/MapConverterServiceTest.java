package com.example.mapsbridge.service;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.ConvertResponse;
import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.exception.InvalidInputException;
import com.example.mapsbridge.provider.MapProvider;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapConverterServiceTest {

    @Mock
    private MapProvider googleProvider;

    @Mock
    private MapProvider appleProvider;

    private MapConverterService service;

    private MapType googleMapType;
    private MapType appleMapType;

    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        // Configure mock providers with lenient strictness
        googleMapType = mock(MapType.class);
        lenient().when(googleMapType.getName()).thenReturn("google");
        lenient().when(googleProvider.getType()).thenReturn(googleMapType);

        lenient().when(googleProvider.generateUrl(any(Coordinate.class)))
                .thenAnswer(i -> "https://www.google.com/maps?q=" + i.getArgument(0, Coordinate.class).getLat() + "," + i.getArgument(0, Coordinate.class).getLon());
        lenient().when(googleProvider.isProviderUrl(startsWith("https://maps.google.com"))).thenReturn(true);
        lenient().when(googleProvider.extractCoordinates("https://maps.google.com/?q=Statue+of+Liberty"))
                .thenReturn(new Coordinate(40.6892, -74.0445));

        appleMapType = mock(MapType.class);
        lenient().when(appleMapType.getName()).thenReturn("apple");
        lenient().when(appleProvider.getType()).thenReturn(appleMapType);

        lenient().when(appleProvider.generateUrl(any(Coordinate.class)))
                .thenAnswer(i -> "https://maps.apple.com/?ll=" + i.getArgument(0, Coordinate.class).getLat() + "," + i.getArgument(0, Coordinate.class).getLon());

        // Initialize Micrometer components
        meterRegistry = new SimpleMeterRegistry();
        Counter.Builder inputTypeCounterBuilder = Counter.builder("maps.input.type")
                .description("Number of times each input type is used (coordinates vs URL)");
        Counter.Builder mapProviderUrlCounterBuilder = Counter.builder("maps.provider.url.usage")
                .description("Number of times URLs from each map provider are used as input");

        // Initialize service with mock providers and Micrometer components
        service = new MapConverterService(List.of(googleProvider, appleProvider),
                inputTypeCounterBuilder,
                mapProviderUrlCounterBuilder,
                meterRegistry);
    }

    @Test
    void testConvertCoordinates() {
        // Given
        ConvertRequest request = new ConvertRequest("40.6892,-74.0445");

        // When
        ConvertResponse response = service.convert(request);

        // Then
        assertNotNull(response);
        assertEquals(40.6892, response.getCoordinates().getLat());
        assertEquals(-74.0445, response.getCoordinates().getLon());
        assertEquals(2, response.getLinks().size());
        assertEquals("https://www.google.com/maps?q=40.6892,-74.0445", response.getLinks().get(googleMapType));
        assertEquals("https://maps.apple.com/?ll=40.6892,-74.0445", response.getLinks().get(appleMapType));

        // Verify metrics
        assertEquals(1, meterRegistry.get("maps.input.type")
                .tag("type", "coordinates")
                .counter().count());
    }

    @Test
    void testConvertGoogleMapsUrl() {
        // Given
        ConvertRequest request = new ConvertRequest("https://maps.google.com/?q=Statue+of+Liberty");

        // When
        ConvertResponse response = service.convert(request);

        // Then
        assertNotNull(response);
        assertEquals(40.6892, response.getCoordinates().getLat());
        assertEquals(-74.0445, response.getCoordinates().getLon());
        assertEquals(2, response.getLinks().size());
        assertEquals("https://www.google.com/maps?q=40.6892,-74.0445", response.getLinks().get(googleMapType));
        assertEquals("https://maps.apple.com/?ll=40.6892,-74.0445", response.getLinks().get(appleMapType));

        // Verify metrics
        assertEquals(1, meterRegistry.get("maps.input.type")
                .tag("type", "url")
                .counter().count());
        assertEquals(1, meterRegistry.get("maps.provider.url.usage")
                .tag("provider", "google")
                .counter().count());
    }

    @Test
    void testInvalidInput() {
        // Given
        ConvertRequest request = new ConvertRequest("invalid input");

        // When/Then
        Exception exception = assertThrows(InvalidInputException.class, () -> service.convert(request));

        assertTrue(exception.getMessage().contains("Input must be coordinates"));
    }
}
