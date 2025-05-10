package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.exception.CoordinateExtractionException;
import com.example.mapsbridge.model.Coordinate;
import com.example.mapsbridge.model.MapType;
import com.example.mapsbridge.service.GoogleGeocodingService;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleMapProviderTest {

    private GoogleMapProvider googleMapProvider;

    @Mock
    private OkHttpClient mockHttpClient;

    @Mock
    private GoogleGeocodingService mockGeocodingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configure mock geocoding service with lenient stubbing to avoid unnecessary stubbing warnings
        lenient().when(mockGeocodingService.isApiEnabled()).thenReturn(false);

        // Provider with mock geocoding service
        googleMapProvider = new GoogleMapProvider(new OkHttpClient.Builder().build(),
                "https://www.google.com/maps?q={lat},{lon}", mockGeocodingService);
    }

    @Test
    void testGetType() {
        assertEquals(MapType.GOOGLE, googleMapProvider.getType());
    }

    @Test
    void testIsProviderUrl() {
        // Standard Google Maps URLs
        assertTrue(googleMapProvider.isProviderUrl("https://www.google.com/maps?q=40.7128,-74.0060"));
        assertTrue(googleMapProvider.isProviderUrl("http://google.com/maps/place/New+York"));

        // Shortened URLs
        assertTrue(googleMapProvider.isProviderUrl("https://maps.app.goo.gl/WbadHecb2EGVBi378"));
        assertTrue(googleMapProvider.isProviderUrl("https://goo.gl/maps/WbadHecb2EGVBi378"));

        // Non-Google URLs
        assertFalse(googleMapProvider.isProviderUrl("https://maps.apple.com/?ll=40.7128,-74.0060"));
        assertFalse(googleMapProvider.isProviderUrl("https://www.openstreetmap.org/#map=15/40.7128/-74.0060"));
    }

    @Test
    void testGenerateUrl() {
        Coordinate coordinate = new Coordinate(40.7128, -74.0060);
        String url = googleMapProvider.generateUrl(coordinate);
        assertEquals("https://www.google.com/maps?q=40.7128,-74.006", url);
    }

    @Test
    void testMobileUnknownLocationURL() {
        // Mobile.UknownLocationURL
        Coordinate coordinate = googleMapProvider.extractCoordinates("https://maps.app.goo.gl/3KRHSbjUDnMhbWg59");
        assertNotNull(coordinate);
        assertEquals(51.9740939, coordinate.getLat());
        assertEquals(5.9016994, coordinate.getLon());
    }

    @Test
    void testMobileKnownPlace() {
        // Mobile.KnownPlace - should return NULL for now
        Coordinate coordinate = googleMapProvider.extractCoordinates("https://maps.app.goo.gl/Hpn85aKXtkAgvzss5?g_st=com.google.maps.preview.copy");
        assertNull(coordinate);
    }

    @Test
    void testPCGoogleMapsKnownPlace() {
        // PCGoogleMaps.KnownPlace
        // Use the real GoogleMapProvider to follow redirects
        Coordinate coordinate = googleMapProvider.extractCoordinates("https://maps.app.goo.gl/f9PD4LmvWrPdhEDT6");
        assertNotNull(coordinate);
        assertEquals(51.9779268, coordinate.getLat());
        assertEquals(5.9055642, coordinate.getLon());
    }

    @Test
    void testPCGoogleMapsUnknownLocationURL() {
        // PCGoogleMaps.UknownLocationURL
        // Use the real GoogleMapProvider to follow redirects
        Coordinate coordinate = googleMapProvider.extractCoordinates("https://maps.app.goo.gl/JPReVmWTAmwegBhA8");
        assertNotNull(coordinate);
        assertEquals(48.114237, coordinate.getLat());
        assertEquals(10.862795, coordinate.getLon());
    }

    @Test
    void testPCGoogleMapsSearchBar() {
        // PCGoogleMaps.SearchBar
        // This URL contains coordinates in the format !3d!4d
        String url = "https://www.google.com/maps/place/St.+Walburga+Kapelle/@48.0698426,10.8694184,13z/data=!4m15!1m8!3m7!1s0x479c23a8dd6e0211:0xa721cdb237f131d6!2sKaufering,+Germany!3b1!8m2!3d48.088056!4d10.8567673!16s%2Fm%2F02q4cwf!3m5!1s0x479c23f7333d7805:0xf3eb5f42c3a31b9d!8m2!3d48.1001328!4d10.8898249!16s%2Fg%2F11f9yfn_x4?entry=ttu&g_ep=EgoyMDI1MDUwNS4wIKXMDSoASAFQAw%3D%3D";

        // Use the real GoogleMapProvider to extract coordinates
        Coordinate coordinate = googleMapProvider.extractCoordinates(url);
        assertNotNull(coordinate);
        assertEquals(48.1001328, coordinate.getLat());
        assertEquals(10.8898249, coordinate.getLon());
    }

    // Note: API-dependent tests are commented out because they're hard to mock correctly
    // These features are tested manually or with integration tests

    /*
    @Test
    void testExtractCoordinatesFromQueryWithApi() throws Exception {
        // This test requires a real API key and network access
    }

    @Test
    void testExtractCoordinatesFromPlaceIdWithApi() throws Exception {
        // This test requires a real API key and network access
    }
    */

    @Test
    void testExtractCoordinatesFromInvalidUrl() {
        // Test null and empty URLs - these should still return null
        assertNull(googleMapProvider.extractCoordinates(""));
        assertNull(googleMapProvider.extractCoordinates(null));
    }
}
