package com.example.mapsbridge.provider.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import okhttp3.OkHttpClient;

import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;

class GoogleMapProviderTest {
    
    private GoogleMapProvider googleMapProvider;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        googleMapProvider = new GoogleMapProvider(new OkHttpClient.Builder().build(),
                "https://www.google.com/maps?q={lat},{lon}", "");
    }
    
    @Test
    void testGetType() {
        assertEquals(MapType.GOOGLE, googleMapProvider.getType());
    }
    
    @Test
    void testIsProviderUrl() {
        assertTrue(googleMapProvider.isProviderUrl("https://www.google.com/maps?q=40.7128,-74.0060"));
        assertTrue(googleMapProvider.isProviderUrl("http://google.com/maps/place/New+York"));
        assertFalse(googleMapProvider.isProviderUrl("https://maps.apple.com/?ll=40.7128,-74.0060"));
    }
    
    @Test
    void testGenerateUrl() {
        Coordinate coordinate = new Coordinate(40.7128, -74.0060);
        String url = googleMapProvider.generateUrl(coordinate);
        assertEquals("https://www.google.com/maps?q=40.7128,-74.006", url);
    }
    
    @Test
    void testExtractCoordinates() {
        Coordinate coordinate = googleMapProvider.extractCoordinates("https://www.google.com/maps?q=40.7128,-74.0060");
        assertNotNull(coordinate);
        assertEquals(40.7128, coordinate.getLat());
        assertEquals(-74.0060, coordinate.getLon());
    }
    
    @Test
    void testExtractCoordinatesFromInvalidUrl() {
        assertNull(googleMapProvider.extractCoordinates("https://www.google.com/maps/place/New+York"));
        assertNull(googleMapProvider.extractCoordinates(""));
        assertNull(googleMapProvider.extractCoordinates(null));
    }
}
