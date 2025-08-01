package com.example.mapsbridge.telegram.service;

import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.dto.request.ConvertRequest;
import com.example.mapsbridge.dto.response.WebConvertResponse;
import com.example.mapsbridge.service.converter.MapConverterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResponseFormatterService.convertMessageToMapLinks method.
 */
@ExtendWith(MockitoExtension.class)
class ResponseFormatterServiceTest {

    @Mock
    private MapConverterServiceImpl mapConverterService;
    
    private ResponseFormatterService responseFormatterService;
    
    @BeforeEach
    void setUp() {
        responseFormatterService = new ResponseFormatterService(mapConverterService);
    }

    @Test
    void testConvertMessageToMapLinks_withCoordinates() {
        // Given
        String message = "40.6892,-74.0445";
        ConvertRequest expectedRequest = new ConvertRequest("40.6892,-74.0445");

        WebConvertResponse mockResponse = new WebConvertResponse();
        Map<MapType, String> links = new HashMap<>();
        links.put(MapType.GOOGLE, "https://www.google.com/maps?q=40.6892,-74.0445");
        links.put(MapType.APPLE, "https://maps.apple.com/?ll=40.6892,-74.0445");
        links.put(MapType.BING, "https://www.bing.com/maps?cp=40.6892~-74.0445");
        links.put(MapType.OPENSTREETMAP, "https://www.openstreetmap.org/?mlat=40.6892&mlon=-74.0445");
        links.put(MapType.WAZE, "https://waze.com/ul?ll=40.6892,-74.0445");
        links.put(MapType.KOMOOT, "https://www.komoot.com/plan/@40.6892,-74.0445");
        mockResponse.setLinks(links);
        mockResponse.setAddress("Statue of Liberty, New York, USA");
        
        when(mapConverterService.convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput()))))
            .thenReturn(mockResponse);

        // When
        String response = responseFormatterService.convertMessageToMapLinks(message);

        // Then
        assertNotNull(response);
        assertTrue(response.contains("Google Maps"));
        assertTrue(response.contains("Apple Maps"));
        assertTrue(response.contains("Bing Maps"));
        assertTrue(response.contains("OpenStreetMap"));
        assertTrue(response.contains("Waze"));
        assertTrue(response.contains("Komoot"));
        
        // Verify links contain the coordinates
        assertTrue(response.contains("href=\"https://www.google.com/maps"));
        assertTrue(response.contains("href=\"https://maps.apple.com"));
        assertTrue(response.contains("href=\"https://www.bing.com/maps"));
        assertTrue(response.contains("href=\"https://www.openstreetmap.org"));
        assertTrue(response.contains("href=\"https://waze.com"));
        assertTrue(response.contains("href=\"https://www.komoot.com"));

        // Verify address is displayed
        assertTrue(response.contains("📍 Statue of Liberty, New York, USA"));
        
        // Verify the service was called with the correct request
        verify(mapConverterService).convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput())));
    }

    @Test
    void testConvertMessageToMapLinks_withGoogleMapsUrl() {
        // Given
        String message = "https://www.google.com/maps?q=40.6892,-74.0445";
        ConvertRequest expectedRequest = new ConvertRequest("https://www.google.com/maps?q=40.6892,-74.0445");

        WebConvertResponse mockResponse = new WebConvertResponse();
        Map<MapType, String> links = new HashMap<>();
        links.put(MapType.GOOGLE, "https://www.google.com/maps?q=40.6892,-74.0445");
        links.put(MapType.APPLE, "https://maps.apple.com/?ll=40.6892,-74.0445");
        links.put(MapType.BING, "https://www.bing.com/maps?cp=40.6892~-74.0445");
        links.put(MapType.OPENSTREETMAP, "https://www.openstreetmap.org/?mlat=40.6892&mlon=-74.0445");
        links.put(MapType.WAZE, "https://waze.com/ul?ll=40.6892,-74.0445");
        links.put(MapType.KOMOOT, "https://www.komoot.com/plan/@40.6892,-74.0445");
        mockResponse.setLinks(links);
        
        when(mapConverterService.convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput()))))
            .thenReturn(mockResponse);

        // When
        String response = responseFormatterService.convertMessageToMapLinks(message);

        // Then
        assertNotNull(response);
        assertTrue(response.contains("Google Maps"));
        assertTrue(response.contains("Apple Maps"));
        assertTrue(response.contains("Bing Maps"));
        assertTrue(response.contains("OpenStreetMap"));
        assertTrue(response.contains("Waze"));
        assertTrue(response.contains("Komoot"));
        
        // Verify the service was called with the correct request
        verify(mapConverterService).convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput())));
    }

    @Test
    void testConvertMessageToMapLinks_withOpenStreetMapUrl() {
        // Given
        String message = "https://www.openstreetmap.org/?mlat=40.6892&mlon=-74.0445#map=16/40.6892/-74.0445";
        ConvertRequest expectedRequest = new ConvertRequest("https://www.openstreetmap.org/?mlat=40.6892&mlon=-74.0445#map=16/40.6892/-74.0445");

        WebConvertResponse mockResponse = new WebConvertResponse();
        Map<MapType, String> links = new HashMap<>();
        links.put(MapType.GOOGLE, "https://www.google.com/maps?q=40.6892,-74.0445");
        links.put(MapType.APPLE, "https://maps.apple.com/?ll=40.6892,-74.0445");
        links.put(MapType.BING, "https://www.bing.com/maps?cp=40.6892~-74.0445");
        links.put(MapType.OPENSTREETMAP, "https://www.openstreetmap.org/?mlat=40.6892&mlon=-74.0445");
        links.put(MapType.WAZE, "https://waze.com/ul?ll=40.6892,-74.0445");
        links.put(MapType.KOMOOT, "https://www.komoot.com/plan/@40.6892,-74.0445");
        mockResponse.setLinks(links);
        
        when(mapConverterService.convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput()))))
            .thenReturn(mockResponse);

        // When
        String response = responseFormatterService.convertMessageToMapLinks(message);

        // Then
        assertNotNull(response);
        assertTrue(response.contains("Google Maps"));
        assertTrue(response.contains("Apple Maps"));
        assertTrue(response.contains("Bing Maps"));
        assertTrue(response.contains("OpenStreetMap"));
        assertTrue(response.contains("Waze"));
        assertTrue(response.contains("Komoot"));
        
        // Verify the service was called with the correct request
        verify(mapConverterService).convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput())));
    }

    @Test
    void testConvertMessageToMapLinks_withInvalidInput() {
        // Given
        String message = "invalid input";
        ConvertRequest expectedRequest = new ConvertRequest("invalid input");
        
        when(mapConverterService.convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput()))))
            .thenThrow(new RuntimeException("Invalid input"));

        // When
        String response = responseFormatterService.convertMessageToMapLinks(message);

        // Then
        assertEquals("Sorry, I couldn't process your message. Please try again later! 🤖", response);
        
        // Verify the service was called with the correct request
        verify(mapConverterService).convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput())));
    }

    @Test
    void testConvertMessageToMapLinks_withEmptyInput() {
        // Given
        String message = "";
        ConvertRequest expectedRequest = new ConvertRequest("");
        
        when(mapConverterService.convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput()))))
            .thenThrow(new RuntimeException("Empty input"));

        // When
        String response = responseFormatterService.convertMessageToMapLinks(message);

        // Then
        assertEquals("Sorry, I couldn't process your message. Please try again later! 🤖", response);
        
        // Verify the service was called with the correct request
        verify(mapConverterService).convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput())));
    }

    @Test
    void testConvertMessageToMapLinks_withWhitespaceInput() {
        // Given
        String message = "   ";
        ConvertRequest expectedRequest = new ConvertRequest(""); // After trim() it becomes empty
        
        when(mapConverterService.convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput()))))
            .thenThrow(new RuntimeException("Empty input"));

        // When
        String response = responseFormatterService.convertMessageToMapLinks(message);

        // Then
        assertEquals("Sorry, I couldn't process your message. Please try again later! 🤖", response);
        
        // Verify the service was called with the correct request
        verify(mapConverterService).convert(argThat(request -> 
            request.getInput().equals(expectedRequest.getInput())));
    }

    @Test
    void testConvertMessageToMapLinks_withNameAndAddressNotContainingName() {
        // Given
        String message = "40.6892,-74.0445";
        ConvertRequest expectedRequest = new ConvertRequest("40.6892,-74.0445");

        WebConvertResponse mockResponse = new WebConvertResponse();
        Map<MapType, String> links = new HashMap<>();
        links.put(MapType.GOOGLE, "https://www.google.com/maps?q=40.6892,-74.0445");
        mockResponse.setLinks(links);
        mockResponse.setName("Statue of Liberty");
        mockResponse.setAddress("New York, USA");

        when(mapConverterService.convert(argThat(request ->
                request.getInput().equals(expectedRequest.getInput()))))
                .thenReturn(mockResponse);

        // When
        String response = responseFormatterService.convertMessageToMapLinks(message);

        // Then
        assertNotNull(response);
        // Verify name is displayed on top
        assertTrue(response.contains("🏷️ Statue of Liberty"));
        // Verify address is displayed below
        assertTrue(response.contains("📍 New York, USA"));
        // Verify the order: name should appear before address
        assertTrue(response.indexOf("Statue of Liberty") < response.indexOf("New York, USA"));
    }

    @Test
    void testConvertMessageToMapLinks_withNameAndAddressContainingName() {
        // Given
        String message = "40.6892,-74.0445";
        ConvertRequest expectedRequest = new ConvertRequest("40.6892,-74.0445");

        WebConvertResponse mockResponse = new WebConvertResponse();
        Map<MapType, String> links = new HashMap<>();
        links.put(MapType.GOOGLE, "https://www.google.com/maps?q=40.6892,-74.0445");
        mockResponse.setLinks(links);
        mockResponse.setName("Statue of Liberty");
        mockResponse.setAddress("Statue of Liberty, New York, USA");

        when(mapConverterService.convert(argThat(request ->
                request.getInput().equals(expectedRequest.getInput()))))
                .thenReturn(mockResponse);

        // When
        String response = responseFormatterService.convertMessageToMapLinks(message);

        // Then
        assertNotNull(response);
        // Verify only address is displayed (not name separately)
        assertTrue(response.contains("📍 Statue of Liberty, New York, USA"));
        // Verify name is not displayed separately
        assertEquals(response.indexOf("📍 Statue of Liberty"), response.lastIndexOf("📍 Statue of Liberty"));
    }
}