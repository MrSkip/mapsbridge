package com.example.mapsbridge.service;

import com.example.mapsbridge.dto.*;
import com.example.mapsbridge.exception.InvalidInputException;
import com.example.mapsbridge.provider.MapProvider;
import com.example.mapsbridge.service.impl.MapConverterServiceImpl;
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

    @Mock
    private UserInputProcessorService userInputProcessorService;

    private MapConverterServiceImpl service;

    private MapType googleMapType;
    private MapType appleMapType;

    @BeforeEach
    void setUp() {
        // Configure mock providers with lenient strictness
        googleMapType = mock(MapType.class);
        lenient().when(googleMapType.getName()).thenReturn("google");
        lenient().when(googleProvider.getType()).thenReturn(googleMapType);

        lenient().when(googleProvider.generateUrl(any(LocationResult.class)))
                .thenAnswer(i -> "https://www.google.com/maps?q=" + i.getArgument(0, LocationResult.class).getCoordinates().getLat() + "," + i.getArgument(0, LocationResult.class).getCoordinates().getLon());

        appleMapType = mock(MapType.class);
        lenient().when(appleMapType.getName()).thenReturn("apple");
        lenient().when(appleProvider.getType()).thenReturn(appleMapType);

        lenient().when(appleProvider.generateUrl(any(LocationResult.class)))
                .thenAnswer(i -> "https://maps.apple.com/?ll=" + i.getArgument(0, LocationResult.class).getCoordinates().getLat() + "," + i.getArgument(0, LocationResult.class).getCoordinates().getLon());

        // Initialize service with mock providers and input processor
        service = new MapConverterServiceImpl(List.of(googleProvider, appleProvider), userInputProcessorService);
    }

    @Test
    void testConvertCoordinates() {
        // Given
        ConvertRequest request = new ConvertRequest("40.6892,-74.0445");
        Coordinate expectedCoordinate = new Coordinate(40.6892, -74.0445);
        LocationResult expectedLocationResult = LocationResult.fromCoordinates(expectedCoordinate);
        when(userInputProcessorService.processInput("40.6892,-74.0445")).thenReturn(expectedLocationResult);

        // When
        ConvertResponse response = service.convert(request);

        // Then
        assertNotNull(response);
        assertEquals(40.6892, response.getCoordinates().getLat());
        assertEquals(-74.0445, response.getCoordinates().getLon());
        assertEquals(2, response.getLinks().size());
        assertEquals("https://www.google.com/maps?q=40.6892,-74.0445", response.getLinks().get(googleMapType));
        assertEquals("https://maps.apple.com/?ll=40.6892,-74.0445", response.getLinks().get(appleMapType));

        // Verify input processor was called
        verify(userInputProcessorService).processInput("40.6892,-74.0445");
    }

    @Test
    void testConvertGoogleMapsUrl() {
        // Given
        ConvertRequest request = new ConvertRequest("https://maps.google.com/?q=Statue+of+Liberty");
        Coordinate expectedCoordinate = new Coordinate(40.6892, -74.0445);
        LocationResult expectedLocationResult = LocationResult.fromCoordinatesAndName(expectedCoordinate, "Statue of Liberty");
        when(userInputProcessorService.processInput("https://maps.google.com/?q=Statue+of+Liberty")).thenReturn(expectedLocationResult);

        // When
        ConvertResponse response = service.convert(request);

        // Then
        assertNotNull(response);
        assertEquals(40.6892, response.getCoordinates().getLat());
        assertEquals(-74.0445, response.getCoordinates().getLon());
        assertEquals(2, response.getLinks().size());
        assertEquals("https://www.google.com/maps?q=40.6892,-74.0445", response.getLinks().get(googleMapType));
        assertEquals("https://maps.apple.com/?ll=40.6892,-74.0445", response.getLinks().get(appleMapType));

        // Verify input processor was called
        verify(userInputProcessorService).processInput("https://maps.google.com/?q=Statue+of+Liberty");
    }

    @Test
    void testInvalidInput() {
        // Given
        ConvertRequest request = new ConvertRequest("invalid input");
        when(userInputProcessorService.processInput("invalid input")).thenThrow(new InvalidInputException("Input must be coordinates (lat,lon) or a valid URL"));

        // When/Then
        Exception exception = assertThrows(InvalidInputException.class, () -> service.convert(request));

        assertTrue(exception.getMessage().contains("Input must be coordinates"));
    }
}
