package com.example.mapsbridge.provider.impl;

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

    private GoogleMapProvider target;

    @Mock
    private GoogleGeocodingService mockGeocodingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        lenient().when(mockGeocodingService.isApiEnabled()).thenReturn(false);

        // Provider with mock geocoding service
        target = new GoogleMapProvider(new OkHttpClient.Builder().build(),
                "https://www.google.com/maps?q={lat},{lon}", mockGeocodingService);
    }

    @Test
    void shouldReturnGoogleMapType() {
        // given
        // provider is initialized in setUp()

        // when
        MapType result = target.getType();

        // then
        assertEquals(MapType.GOOGLE, result);
    }

    @Test
    void shouldIdentifyGoogleMapsUrls() {
        // given
        // provider is initialized in setUp()

        // when & then
        // Standard Google Maps URLs
        assertTrue(target.isProviderUrl("https://www.google.com/maps?q=40.7128,-74.0060"));
        assertTrue(target.isProviderUrl("http://google.com/maps/place/New+York"));

        // Shortened URLs
        assertTrue(target.isProviderUrl("https://maps.app.goo.gl/WbadHecb2EGVBi378"));
        assertTrue(target.isProviderUrl("https://goo.gl/maps/WbadHecb2EGVBi378"));

        // Non-Google URLs
        assertFalse(target.isProviderUrl("https://maps.apple.com/?ll=40.7128,-74.0060"));
        assertFalse(target.isProviderUrl("https://www.openstreetmap.org/#map=15/40.7128/-74.0060"));
    }

    @Test
    void shouldGenerateCorrectGoogleMapsUrl() {
        // given
        Coordinate coordinate = new Coordinate(40.7128, -74.0060);

        // when
        String url = target.generateUrl(coordinate);

        // then
        assertEquals("https://www.google.com/maps?q=40.7128,-74.006", url);
    }

    @Test
    void shouldExtractCoordinatesFromMobileUnknownLocationUrl() {
        // given
        String mobileUnknownLocationUrl = "https://maps.app.goo.gl/3KRHSbjUDnMhbWg59";

        // when
        Coordinate coordinate = target.extractCoordinates(mobileUnknownLocationUrl);

        // then
        assertNotNull(coordinate);
        assertEquals(51.9740939, coordinate.getLat());
        assertEquals(5.9016994, coordinate.getLon());
    }

    @Test
    void shouldReturnNullForMobileKnownPlaceUrl() {
        // given
        String mobileKnownPlaceUrl = "https://maps.app.goo.gl/Hpn85aKXtkAgvzss5?g_st=com.google.maps.preview.copy";

        // when
        Coordinate coordinate = target.extractCoordinates(mobileKnownPlaceUrl);

        // then
        assertNull(coordinate);
    }

    @Test
    void shouldExtractCoordinatesFromPCGoogleMapsKnownPlace() {
        // given
        String pcGoogleMapsKnownPlaceUrl = "https://maps.app.goo.gl/f9PD4LmvWrPdhEDT6";

        // when
        Coordinate coordinate = target.extractCoordinates(pcGoogleMapsKnownPlaceUrl);

        // then
        assertNotNull(coordinate);
        assertEquals(51.9779268, coordinate.getLat());
        assertEquals(5.9055642, coordinate.getLon());
    }

    @Test
    void shouldExtractCoordinatesFromPCGoogleMapsUnknownLocationUrl() {
        // given
        String pcGoogleMapsUnknownLocationUrl = "https://maps.app.goo.gl/JPReVmWTAmwegBhA8";

        // when
        Coordinate coordinate = target.extractCoordinates(pcGoogleMapsUnknownLocationUrl);

        // then
        assertNotNull(coordinate);
        assertEquals(48.114237, coordinate.getLat());
        assertEquals(10.862795, coordinate.getLon());
    }

    @Test
    void shouldExtractCoordinatesFromPCGoogleMapsSearchBarUrl() {
        // given
        // This URL contains coordinates in the format !3d!4d
        String pcGoogleMapsSearchBarUrl = "https://www.google.com/maps/place/St.+Walburga+Kapelle/@48.0698426,10.8694184,13z/data=!4m15!1m8!3m7!1s0x479c23a8dd6e0211:0xa721cdb237f131d6!2sKaufering,+Germany!3b1!8m2!3d48.088056!4d10.8567673!16s%2Fm%2F02q4cwf!3m5!1s0x479c23f7333d7805:0xf3eb5f42c3a31b9d!8m2!3d48.1001328!4d10.8898249!16s%2Fg%2F11f9yfn_x4?entry=ttu&g_ep=EgoyMDI1MDUwNS4wIKXMDSoASAFQAw%3D%3D";

        // when
        Coordinate coordinate = target.extractCoordinates(pcGoogleMapsSearchBarUrl);

        // then
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
    void shouldReturnNullWhenExtractingCoordinatesFromInvalidUrl() {
        // given
        String emptyUrl = "";
        String nullUrl = null;

        // when & then
        assertNull(target.extractCoordinates(emptyUrl));
        assertNull(target.extractCoordinates(nullUrl));
    }

    @Test
    void shouldExtractCoordinatesUsingGeocodingServiceForQueryUrl() {
        // given
        when(mockGeocodingService.isApiEnabled()).thenReturn(true);

        String urlWithQuery = "https://www.google.com/maps?q=New+York";
        Coordinate expectedCoordinate = new Coordinate(40.7128, -74.0060);

        // Configure mock to return coordinates for the query
        // Note: GoogleMapProvider replaces '+' with ' ' before calling the service
        when(mockGeocodingService.geocodeQuery("New York")).thenReturn(expectedCoordinate);

        // when
        Coordinate result = target.extractCoordinates(urlWithQuery);

        // then
        assertNotNull(result);
        assertEquals(expectedCoordinate.getLat(), result.getLat());
        assertEquals(expectedCoordinate.getLon(), result.getLon());

        // Verify the service was called
        verify(mockGeocodingService).geocodeQuery("New York");
    }

    @Test
    void shouldExtractCoordinatesUsingGeocodingServiceForPlaceIdUrl() {
        // given
        when(mockGeocodingService.isApiEnabled()).thenReturn(true);
        String urlWithPlaceId = "https://www.google.com/maps/place/Statue+of+Liberty/data=!4m6!3m5!1s0x89c25090129c363d:0x40c6a5770d25022b";

        Coordinate expectedCoordinate = new Coordinate(40.6892494, -74.0445004);

        // Configure mock to return coordinates for the place ID
        when(mockGeocodingService.getPlaceCoordinates("0x89c25090129c363d:0x40c6a5770d25022b")).thenReturn(expectedCoordinate);

        // when
        Coordinate result = target.extractCoordinates(urlWithPlaceId);

        // then
        assertNotNull(result);
        assertEquals(expectedCoordinate.getLat(), result.getLat());
        assertEquals(expectedCoordinate.getLon(), result.getLon());

        // Verify the service was called
        verify(mockGeocodingService).getPlaceCoordinates("0x89c25090129c363d:0x40c6a5770d25022b");
    }

    @Test
    void shouldExtractPlaceIdFromUrlWithPlaceIdParameter() {
        // given
        // Test the first pattern: place_id= parameter
        String urlWithPlaceIdParameter = "https://www.google.com/maps/place/New+York+City,+NY,+USA/@40.7127753,-74.0059728,12z/data=!3m1!4b1!4m6!3m5!1s0x89c24fa5d33f083b:0xc80b8f06e177fe62!8m2!3d40.7127753!4d-74.0059728!16zL20vMDJfMjg2?entry=ttu&place_id=ChIJOwg_06VPwokRYv534QaPC8g";

        // when
        String placeId = target.findPlaceId(urlWithPlaceIdParameter);

        // then
        assertNotNull(placeId);
        assertEquals("ChIJOwg_06VPwokRYv534QaPC8g", placeId);
    }

    @Test
    void shouldExtractPlaceIdFromUrlWith1sPattern() {
        // given
        // Test the second pattern: !1s pattern
        String urlWith1sPattern = "https://www.google.com/maps/place/New+York+City/@40.7127753,-74.0059728,12z/data=!1sChIJOwg_06VPwokRYv534QaPC8g!2m1!3m1!1s0x89c24fa5d33f083b:0xc80b8f06e177fe62";

        // when
        String placeId = target.findPlaceId(urlWith1sPattern);

        // then
        assertNotNull(placeId);
        assertEquals("ChIJOwg_06VPwokRYv534QaPC8g", placeId);
    }

    @Test
    void shouldExtractPlaceIdFromUrlWith3m1sPattern() {
        // given
        // Test the third pattern: !3m\d+!1s
        String urlWith3m1sPattern = "https://www.google.com/maps/place/Statue+of+Liberty/data=!4m6!3m5!1s0x89c25090129c363d:0x40c6a5770d25022b!8m2!3d40.6892494!4d-74.0445004!16s%2Fm%2F072p8";

        // when
        String placeId = target.findPlaceId(urlWith3m1sPattern);

        // then
        assertNotNull(placeId);
        assertEquals("0x89c25090129c363d:0x40c6a5770d25022b", placeId);
    }
}
