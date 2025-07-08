package com.example.mapsbridge.service;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.ConvertResponse;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.service.impl.MapConverterServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MapConverterService.convert method.
 * These tests use real implementations of all dependencies (no mocks).
 */
@SpringBootTest
class MapConverterServiceIntegrationTest {

    @Autowired
    private MapConverterServiceImpl mapConverterService;

    @Test
    void testConvertCoordinates() {
        // Given
        ConvertRequest request = new ConvertRequest("40.6892,-74.0445");

        // When
        ConvertResponse response = mapConverterService.convert(request);

        // Then
        assertNotNull(response);
        assertEquals(40.6892, response.getCoordinates().getLat());
        assertEquals(-74.0445, response.getCoordinates().getLon());
        
        // Verify links for all supported map types
        assertNotNull(response.getLinks().get(MapType.GOOGLE));
        assertNotNull(response.getLinks().get(MapType.APPLE));
        assertNotNull(response.getLinks().get(MapType.BING));
        assertNotNull(response.getLinks().get(MapType.OPENSTREETMAP));
        assertNotNull(response.getLinks().get(MapType.WAZE));
        assertNotNull(response.getLinks().get(MapType.KOMOOT));
        
        // Verify the format of the links
        assertTrue(response.getLinks().get(MapType.GOOGLE).contains("40.6892,-74.0445"));
        assertTrue(response.getLinks().get(MapType.APPLE).contains("40.6892,-74.0445"));
        assertTrue(response.getLinks().get(MapType.BING).contains("40.6892,-74.0445"));
        assertTrue(response.getLinks().get(MapType.OPENSTREETMAP).contains("40.6892"));
        assertTrue(response.getLinks().get(MapType.WAZE).contains("40.6892,-74.0445"));
        assertTrue(response.getLinks().get(MapType.KOMOOT).contains("40.6892,-74.0445"));
    }

    @Test
    void testConvertGoogleMapsUrl() {
        // Given
        ConvertRequest request = new ConvertRequest("https://www.google.com/maps?q=40.6892,-74.0445");

        // When
        ConvertResponse response = mapConverterService.convert(request);

        // Then
        assertNotNull(response);
        assertEquals(40.6892, response.getCoordinates().getLat());
        assertEquals(-74.0445, response.getCoordinates().getLon());
        
        // Verify links for all supported map types
        assertNotNull(response.getLinks().get(MapType.GOOGLE));
        assertNotNull(response.getLinks().get(MapType.APPLE));
        assertNotNull(response.getLinks().get(MapType.BING));
        assertNotNull(response.getLinks().get(MapType.OPENSTREETMAP));
        assertNotNull(response.getLinks().get(MapType.WAZE));
        assertNotNull(response.getLinks().get(MapType.KOMOOT));
    }

    @Test
    void testConvertOpenStreetMapUrl() {
        // Given
        ConvertRequest request = new ConvertRequest("https://www.openstreetmap.org/?mlat=40.6892&mlon=-74.0445#map=16/40.6892/-74.0445");

        // When
        ConvertResponse response = mapConverterService.convert(request);

        // Then
        assertNotNull(response);
        assertEquals(40.6892, response.getCoordinates().getLat());
        assertEquals(-74.0445, response.getCoordinates().getLon());
        
        // Verify links for all supported map types
        assertNotNull(response.getLinks().get(MapType.GOOGLE));
        assertNotNull(response.getLinks().get(MapType.APPLE));
        assertNotNull(response.getLinks().get(MapType.BING));
        assertNotNull(response.getLinks().get(MapType.OPENSTREETMAP));
        assertNotNull(response.getLinks().get(MapType.WAZE));
        assertNotNull(response.getLinks().get(MapType.KOMOOT));
    }

    @Test
    void testConvertWazeUrl() {
        // Given
        ConvertRequest request = new ConvertRequest("https://waze.com/ul?ll=40.6892,-74.0445&navigate=yes");

        // When
        ConvertResponse response = mapConverterService.convert(request);

        // Then
        assertNotNull(response);
        assertEquals(40.6892, response.getCoordinates().getLat());
        assertEquals(-74.0445, response.getCoordinates().getLon());
        
        // Verify links for all supported map types
        assertNotNull(response.getLinks().get(MapType.GOOGLE));
        assertNotNull(response.getLinks().get(MapType.APPLE));
        assertNotNull(response.getLinks().get(MapType.BING));
        assertNotNull(response.getLinks().get(MapType.OPENSTREETMAP));
        assertNotNull(response.getLinks().get(MapType.WAZE));
        assertNotNull(response.getLinks().get(MapType.KOMOOT));
    }
}