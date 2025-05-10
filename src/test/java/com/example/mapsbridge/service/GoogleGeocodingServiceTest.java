package com.example.mapsbridge.service;

import com.example.mapsbridge.model.Coordinate;
import com.google.maps.GeoApiContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GoogleGeocodingServiceTest {

    @Mock
    private GeoApiContext mockGeoApiContext;

    private GoogleGeocodingService geocodingService;
    private GoogleGeocodingService disabledService;

    @BeforeEach
    void setUp() {
        // Create service with API enabled
        geocodingService = new GoogleGeocodingService(mockGeoApiContext, true);

        // Create service with API disabled
        disabledService = new GoogleGeocodingService(mockGeoApiContext, false);
    }

    @Test
    void testApiEnabledFlag() {
        // Verify API is enabled with valid key
        assertTrue(geocodingService.isApiEnabled());

        // Verify API is disabled with empty key
        assertFalse(disabledService.isApiEnabled());
    }

    @Test
    void testNullInputs() {
        // Verify methods handle null inputs
        assertNull(geocodingService.geocodeQuery(null));
        assertNull(geocodingService.geocodeQuery(""));
        assertNull(geocodingService.getPlaceCoordinates(null));
        assertNull(geocodingService.getPlaceCoordinates(""));
    }

    @Test
    void testDisabledServiceReturnsNull() {
        // Verify methods return null when API is disabled
        assertNull(disabledService.geocodeQuery("test"));
        assertNull(disabledService.getPlaceCoordinates("test"));
    }

    // Note: Full integration tests with the actual Google API would require a valid API key
    // and network access, which is not suitable for unit tests.
    // In a real-world scenario, you might want to add integration tests that run
    // with a real API key in a controlled environment.
}
