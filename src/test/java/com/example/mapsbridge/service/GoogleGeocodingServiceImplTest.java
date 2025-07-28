package com.example.mapsbridge.service;

import com.example.mapsbridge.config.metrics.tracker.GeocodingTracker;
import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.service.geocoding.GoogleGeocodingServiceImpl;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleGeocodingServiceImplTest {

    @Mock
    private GeoApiContext geoApiContext;

    @Mock
    private GeocodingTracker geocodingTracker;

    private GoogleGeocodingServiceImpl service;
    private GoogleGeocodingServiceImpl disabledService;

    @BeforeEach
    void setUp() {
        service = new GoogleGeocodingServiceImpl(
                geoApiContext,
                true, // enabled
                geocodingTracker
        );

        disabledService = new GoogleGeocodingServiceImpl(
                geoApiContext,
                false, // disabled
                geocodingTracker
        );
    }

    @Test
    void shouldReturnEnabledStatus() {
        assertTrue(service.isEnabled());
        assertFalse(disabledService.isEnabled());
    }

    // Reverse Geocoding Tests
    @Test
    void shouldReverseGeocodeSuccessfully() throws Exception {
        // Given
        Coordinate coordinate = new Coordinate(40.7128, -74.0060);
        GeocodingResult result = createGeocodingResult("New York, NY", 40.7128, -74.0060);

        try (MockedStatic<GeocodingApi> mockedApi = mockStatic(GeocodingApi.class)) {
            // Create mock request
            com.google.maps.GeocodingApiRequest mockRequest = mock(com.google.maps.GeocodingApiRequest.class);
            when(mockRequest.await()).thenReturn(new GeocodingResult[]{result});

            mockedApi.when(() -> GeocodingApi.reverseGeocode(any(GeoApiContext.class), any(LatLng.class)))
                    .thenReturn(mockRequest);

            // When
            LocationResult locationResult = service.reverseGeocode(coordinate);

            // Then
            assertNotNull(locationResult);
            assertNotNull(locationResult.getCoordinates());
            assertEquals(40.7128, locationResult.getCoordinates().getLat());
            assertEquals(-74.0060, locationResult.getCoordinates().getLon());
            assertEquals("New York, NY", locationResult.getAddress());
            verify(geocodingTracker).trackReverseGeocode("google");
        }
    }

    @Test
    void shouldReturnCoordinatesOnlyWhenReverseGeocodingFails() throws Exception {
        // Given
        Coordinate coordinate = new Coordinate(40.7128, -74.0060);

        try (MockedStatic<GeocodingApi> mockedApi = mockStatic(GeocodingApi.class)) {
            // Create mock request
            com.google.maps.GeocodingApiRequest mockRequest = mock(com.google.maps.GeocodingApiRequest.class);
            when(mockRequest.await()).thenReturn(new GeocodingResult[0]);

            mockedApi.when(() -> GeocodingApi.reverseGeocode(any(GeoApiContext.class), any(LatLng.class)))
                    .thenReturn(mockRequest);

            // When
            LocationResult locationResult = service.reverseGeocode(coordinate);

            // Then
            assertNotNull(locationResult);
            assertNotNull(locationResult.getCoordinates());
            assertEquals(40.7128, locationResult.getCoordinates().getLat());
            assertEquals(-74.0060, locationResult.getCoordinates().getLon());
            assertNull(locationResult.getAddress());
            verify(geocodingTracker).trackReverseGeocode("google");
        }
    }

    @Test
    void shouldHandleReverseGeocodingException() throws Exception {
        // Given
        Coordinate coordinate = new Coordinate(40.7128, -74.0060);

        try (MockedStatic<GeocodingApi> mockedApi = mockStatic(GeocodingApi.class)) {
            // Create mock request that throws exception
            com.google.maps.GeocodingApiRequest mockRequest = mock(com.google.maps.GeocodingApiRequest.class);
            when(mockRequest.await()).thenThrow(new RuntimeException("API Error"));

            mockedApi.when(() -> GeocodingApi.reverseGeocode(any(GeoApiContext.class), any(LatLng.class)))
                    .thenReturn(mockRequest);

            // When
            LocationResult locationResult = service.reverseGeocode(coordinate);

            // Then
            assertNotNull(locationResult);
            assertNotNull(locationResult.getCoordinates());
            assertEquals(40.7128, locationResult.getCoordinates().getLat());
            assertEquals(-74.0060, locationResult.getCoordinates().getLon());
            assertNull(locationResult.getAddress());
            verify(geocodingTracker, never()).trackReverseGeocode(anyString());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false})
    void shouldReturnCoordinatesWhenServiceDisabled(boolean enabled) {
        // Given
        GoogleGeocodingServiceImpl disabledService = new GoogleGeocodingServiceImpl(
                geoApiContext, enabled, geocodingTracker);
        Coordinate coordinate = new Coordinate(40.7128, -74.0060);

        // When
        LocationResult result = disabledService.reverseGeocode(coordinate);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(coordinate.getLat(), result.getCoordinates().getLat());
        assertEquals(coordinate.getLon(), result.getCoordinates().getLon());
        assertNull(result.getAddress());
        verify(geocodingTracker, never()).trackReverseGeocode(anyString());
    }

    // Place ID Tests
    @Test
    void shouldGetLocationFromPlaceIdSuccessfully() throws Exception {
        // Given
        String placeId = "ChIJD7fiBh9u5kcRYJSMaMOCCwQ";
        PlaceDetails placeDetails = createPlaceDetails("Paris, France", "Paris", 48.8566, 2.3522);

        try (MockedStatic<PlacesApi> mockedApi = mockStatic(PlacesApi.class)) {
            // Create mock request
            com.google.maps.PlaceDetailsRequest mockRequest = mock(com.google.maps.PlaceDetailsRequest.class);
            when(mockRequest.await()).thenReturn(placeDetails);

            mockedApi.when(() -> PlacesApi.placeDetails(any(GeoApiContext.class), anyString()))
                    .thenReturn(mockRequest);

            // When
            LocationResult result = service.getLocationFromPlaceId(placeId);

            // Then
            assertNotNull(result);
            assertNotNull(result.getCoordinates());
            assertEquals(48.8566, result.getCoordinates().getLat());
            assertEquals(2.3522, result.getCoordinates().getLon());
            assertEquals("Paris, France", result.getAddress());
            assertEquals("Paris", result.getPlaceName());
            verify(geocodingTracker).trackPlaceIdLookup("google");
        }
    }

    @Test
    void shouldReturnNullForInvalidPlaceId() {
        // When
        LocationResult result = service.getLocationFromPlaceId("");

        // Then
        assertNull(result);
        verify(geocodingTracker, never()).trackPlaceIdLookup(anyString());
    }

    @Test
    void shouldHandlePlaceIdException() throws Exception {
        // Given
        String placeId = "invalid-place-id";

        try (MockedStatic<PlacesApi> mockedApi = mockStatic(PlacesApi.class)) {
            // Create mock request that throws exception
            com.google.maps.PlaceDetailsRequest mockRequest = mock(com.google.maps.PlaceDetailsRequest.class);
            when(mockRequest.await()).thenThrow(new RuntimeException("Invalid place ID"));

            mockedApi.when(() -> PlacesApi.placeDetails(any(GeoApiContext.class), anyString()))
                    .thenReturn(mockRequest);

            // When
            LocationResult result = service.getLocationFromPlaceId(placeId);

            // Then
            assertNull(result);
            verify(geocodingTracker, never()).trackPlaceIdLookup(anyString());
        }
    }

    // Forward Geocoding Tests
    @Test
    void shouldGeocodeQuerySuccessfully() throws Exception {
        // Given
        String query = "New York City";
        GeocodingResult result = createGeocodingResult("New York, NY, USA", 40.7128, -74.0060);

        try (MockedStatic<GeocodingApi> mockedApi = mockStatic(GeocodingApi.class)) {
            // Create mock request
            com.google.maps.GeocodingApiRequest mockRequest = mock(com.google.maps.GeocodingApiRequest.class);
            when(mockRequest.await()).thenReturn(new GeocodingResult[]{result});

            mockedApi.when(() -> GeocodingApi.geocode(any(GeoApiContext.class), anyString()))
                    .thenReturn(mockRequest);

            // When
            LocationResult locationResult = service.geocodeQuery(query);

            // Then
            assertNotNull(locationResult);
            assertNotNull(locationResult.getCoordinates());
            assertEquals(40.7128, locationResult.getCoordinates().getLat());
            assertEquals(-74.0060, locationResult.getCoordinates().getLon());
            assertEquals("New York, NY, USA", locationResult.getAddress());
            verify(geocodingTracker).trackForwardGeocode("google");
        }
    }

    @Test
    void shouldReturnNullForEmptyGeocodingResults() throws Exception {
        // Given
        String query = "nonexistent place";

        try (MockedStatic<GeocodingApi> mockedApi = mockStatic(GeocodingApi.class)) {
            // Create mock request
            com.google.maps.GeocodingApiRequest mockRequest = mock(com.google.maps.GeocodingApiRequest.class);
            when(mockRequest.await()).thenReturn(new GeocodingResult[0]);

            mockedApi.when(() -> GeocodingApi.geocode(any(GeoApiContext.class), anyString()))
                    .thenReturn(mockRequest);

            // When
            LocationResult result = service.geocodeQuery(query);

            // Then
            assertNull(result);
            verify(geocodingTracker).trackForwardGeocode("google");
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldReturnNullForInvalidQuery(String query) {
        // When
        LocationResult result = service.geocodeQuery(query);

        // Then
        assertNull(result);
        verify(geocodingTracker, never()).trackForwardGeocode(anyString());
    }

    // Place Coordinates Tests
    @Test
    void shouldGetPlaceCoordinatesSuccessfully() throws Exception {
        // Given
        String placeId = "ChIJD7fiBh9u5kcRYJSMaMOCCwQ";
        PlaceDetails placeDetails = createPlaceDetailsWithGeometry(48.8566, 2.3522);

        try (MockedStatic<PlacesApi> mockedApi = mockStatic(PlacesApi.class)) {
            // Create the mock request that will be returned by PlacesApi.placeDetails()
            com.google.maps.PlaceDetailsRequest mockRequest = mock(com.google.maps.PlaceDetailsRequest.class);

            // Mock the chaining: PlacesApi.placeDetails().fields().await()
            when(mockRequest.fields(any())).thenReturn(mockRequest);
            when(mockRequest.await()).thenReturn(placeDetails);

            // Mock the static method call
            mockedApi.when(() -> PlacesApi.placeDetails(any(GeoApiContext.class), anyString()))
                    .thenReturn(mockRequest);

            // When
            Coordinate coordinate = service.getPlaceCoordinates(placeId);

            // Then
            assertNotNull(coordinate);
            assertEquals(48.8566, coordinate.getLat());
            assertEquals(2.3522, coordinate.getLon());
            verify(geocodingTracker).trackPlaceIdLookup("google");
        }
    }

    @Test
    void shouldReturnNullForInvalidPlaceIdInGetCoordinates() {
        // When
        Coordinate coordinate = service.getPlaceCoordinates("");

        // Then
        assertNull(coordinate);
        verify(geocodingTracker, never()).trackPlaceIdLookup(anyString());
    }

    @Test
    void shouldHandleGetPlaceCoordinatesException() throws Exception {
        // Given
        String placeId = "invalid-place-id";

        try (MockedStatic<PlacesApi> mockedApi = mockStatic(PlacesApi.class)) {
            // Create mock request that throws exception
            com.google.maps.PlaceDetailsRequest mockRequest = mock(com.google.maps.PlaceDetailsRequest.class);
            when(mockRequest.fields(any())).thenReturn(mockRequest);
            when(mockRequest.await()).thenThrow(new RuntimeException("API Error"));

            mockedApi.when(() -> PlacesApi.placeDetails(any(GeoApiContext.class), anyString()))
                    .thenReturn(mockRequest);

            // When
            Coordinate coordinate = service.getPlaceCoordinates(placeId);

            // Then
            assertNull(coordinate);
            verify(geocodingTracker, never()).trackPlaceIdLookup(anyString());
        }
    }

    // Edge Cases and Validation Tests
    @Test
    void shouldHandleNullCoordinateInReverseGeocode() {
        // When
        LocationResult result = service.reverseGeocode(null);

        // Then
        assertNotNull(result);
        assertNull(result.getCoordinates());
        verify(geocodingTracker, never()).trackReverseGeocode(anyString());
    }

    @Test
    void shouldHandleInvalidCoordinateInReverseGeocode() {
        // Given
        Coordinate invalidCoordinate = new Coordinate(91.0, 181.0); // Invalid lat/lon

        // When
        LocationResult result = service.reverseGeocode(invalidCoordinate);

        // Then
        assertNotNull(result);
        assertEquals(invalidCoordinate, result.getCoordinates());
        verify(geocodingTracker, never()).trackReverseGeocode(anyString());
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 0.0",
            "90.0, 180.0",
            "-90.0, -180.0",
            "45.0, 0.0",
            "0.0, 90.0"
    })
    void shouldHandleValidCoordinateRanges(double lat, double lon) throws Exception {
        // Given
        Coordinate coordinate = new Coordinate(lat, lon);
        GeocodingResult result = createGeocodingResult("Test Location", lat, lon);

        try (MockedStatic<GeocodingApi> mockedApi = mockStatic(GeocodingApi.class)) {
            // Create mock request
            com.google.maps.GeocodingApiRequest mockRequest = mock(com.google.maps.GeocodingApiRequest.class);
            when(mockRequest.await()).thenReturn(new GeocodingResult[]{result});

            mockedApi.when(() -> GeocodingApi.reverseGeocode(any(GeoApiContext.class), any(LatLng.class)))
                    .thenReturn(mockRequest);

            // When
            LocationResult locationResult = service.reverseGeocode(coordinate);

            // Then
            assertNotNull(locationResult);
            assertNotNull(locationResult.getCoordinates());
            assertEquals(lat, locationResult.getCoordinates().getLat());
            assertEquals(lon, locationResult.getCoordinates().getLon());
            verify(geocodingTracker).trackReverseGeocode("google");
        }
    }

    // Helper methods for creating test data
    private GeocodingResult createGeocodingResult(String address, double lat, double lon) {
        GeocodingResult result = new GeocodingResult();
        result.formattedAddress = address;
        result.geometry = new Geometry();
        result.geometry.location = new LatLng(lat, lon);
        return result;
    }

    private PlaceDetails createPlaceDetails(String address, String name, double lat, double lon) {
        PlaceDetails details = new PlaceDetails();
        details.formattedAddress = address;
        details.name = name;
        details.geometry = new Geometry();
        details.geometry.location = new LatLng(lat, lon);
        return details;
    }

    private PlaceDetails createPlaceDetailsWithGeometry(double lat, double lon) {
        PlaceDetails details = new PlaceDetails();
        details.geometry = new Geometry();
        details.geometry.location = new LatLng(lat, lon);
        return details;
    }
}