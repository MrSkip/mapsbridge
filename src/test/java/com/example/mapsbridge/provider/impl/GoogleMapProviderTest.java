package com.example.mapsbridge.provider.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.provider.extractor.impl.*;
import com.example.mapsbridge.provider.extractor.impl.url.UrlPatternExtractor;
import com.example.mapsbridge.service.GoogleGeocodingService;
import com.example.mapsbridge.service.geocoding.HybridGeocodingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleMapProviderTest {

    private GoogleMapProvider target;

    @Mock
    private GoogleGeocodingService mockGeocodingService;

    @Mock
    private HybridGeocodingService mockHybridGeocodingService;

    @Mock
    private UrlPatternExtractor mockUrlPatternExtractor;

    @Mock
    private OkHttpClient mockHttpClient;

    @Mock
    private Counter.Builder mockCounterBuilder;

    @Mock
    private Counter.Builder mockInputTypeCounterBuilder;

    @Mock
    private MeterRegistry mockMeterRegistry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configure mock counter builder
        lenient().when(mockCounterBuilder.tag(anyString(), anyString())).thenReturn(mockCounterBuilder);
        lenient().when(mockCounterBuilder.register(mockMeterRegistry)).thenReturn(mock(Counter.class));

        lenient().when(mockGeocodingService.isApiEnabled()).thenReturn(false);

        // Create mock extractors
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        G2LatLon3d4dExtractor latLon3d4dExtractor = new G2LatLon3d4dExtractor(mockCounterBuilder, mockMeterRegistry);
        G3AtSymbolExtractor atSymbolExtractor = new G3AtSymbolExtractor(mockCounterBuilder, mockMeterRegistry);
        G4QParameterExtractor qParameterExtractor = new G4QParameterExtractor(mockCounterBuilder, mockMeterRegistry);
        G5SearchPatternExtractor searchPatternExtractor = new G5SearchPatternExtractor(mockCounterBuilder, mockMeterRegistry);
        G6PlaceIdExtractor placeIdExtractor = new G6PlaceIdExtractor(
                mockHybridGeocodingService,
                mockUrlPatternExtractor,
                mockCounterBuilder,
                mockMeterRegistry);
        G7AddressGeocodingExtractor addressGeocodingExtractor = new G7AddressGeocodingExtractor(
                mockHybridGeocodingService,
                mockUrlPatternExtractor,
                mockCounterBuilder,
                mockMeterRegistry);

        // Provider with mock geocoding service and extractors
        target = new GoogleMapProvider(
                httpClient,
                "https://www.google.com/maps?q={lat},{lon}",
                Arrays.asList(
                    latLon3d4dExtractor,
                    atSymbolExtractor,
                    qParameterExtractor,
                    searchPatternExtractor,
                        placeIdExtractor,
                        addressGeocodingExtractor
                ),
                mockCounterBuilder,
                mockMeterRegistry);
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
        LocationResult locationResult = target.extractLocation(mobileUnknownLocationUrl);

        // then
        assertNotNull(locationResult);
        assertNotNull(locationResult.getCoordinates());
        assertEquals(51.9740939, locationResult.getCoordinates().getLat());
        assertEquals(5.9016994, locationResult.getCoordinates().getLon());
    }

    @Test
    void shouldReturnNullForMobileKnownPlaceUrl() {
        // given
        String mobileKnownPlaceUrl = "https://maps.app.goo.gl/Hpn85aKXtkAgvzss5?g_st=com.google.maps.preview.copy";

        // when
        LocationResult locationResult = target.extractLocation(mobileKnownPlaceUrl);

        // then
        assertNull(locationResult);
    }

    @Test
    void shouldExtractCoordinatesFromPCGoogleMapsKnownPlace() {
        // given
        String pcGoogleMapsKnownPlaceUrl = "https://maps.app.goo.gl/f9PD4LmvWrPdhEDT6";

        // when
        LocationResult locationResult = target.extractLocation(pcGoogleMapsKnownPlaceUrl);

        // then
        assertNotNull(locationResult);
        assertNotNull(locationResult.getCoordinates());
        assertEquals(51.9779268, locationResult.getCoordinates().getLat());
        assertEquals(5.9055642, locationResult.getCoordinates().getLon());
    }

    @Test
    void shouldExtractCoordinatesFromPCGoogleMapsUnknownLocationUrl() {
        // given
        String pcGoogleMapsUnknownLocationUrl = "https://maps.app.goo.gl/JPReVmWTAmwegBhA8";

        // when
        LocationResult locationResult = target.extractLocation(pcGoogleMapsUnknownLocationUrl);

        // then
        assertNotNull(locationResult);
        assertNotNull(locationResult.getCoordinates());
        assertEquals(48.114237, locationResult.getCoordinates().getLat());
        assertEquals(10.862795, locationResult.getCoordinates().getLon());
    }

    @Test
    void shouldExtractCoordinatesFromPCGoogleMapsSearchBarUrl() {
        // given
        // This URL contains coordinates in the format !3d!4d
        String pcGoogleMapsSearchBarUrl = "https://www.google.com/maps/place/St.+Walburga+Kapelle/@48.0698426,10.8694184,13z/data=!4m15!1m8!3m7!1s0x479c23a8dd6e0211:0xa721cdb237f131d6!2sKaufering,+Germany!3b1!8m2!3d48.088056!4d10.8567673!16s%2Fm%2F02q4cwf!3m5!1s0x479c23f7333d7805:0xf3eb5f42c3a31b9d!8m2!3d48.1001328!4d10.8898249!16s%2Fg%2F11f9yfn_x4?entry=ttu&g_ep=EgoyMDI1MDUwNS4wIKXMDSoASAFQAw%3D%3D";

        // when
        LocationResult locationResult = target.extractLocation(pcGoogleMapsSearchBarUrl);

        // then
        assertNotNull(locationResult);
        assertNotNull(locationResult.getCoordinates());
        assertEquals(48.1001328, locationResult.getCoordinates().getLat());
        assertEquals(10.8898249, locationResult.getCoordinates().getLon());
    }

    @Test
    void shouldReturnNullWhenExtractingCoordinatesFromInvalidUrl() {
        // given
        String emptyUrl = "";
        String nullUrl = null;

        // when & then
        assertNull(target.extractLocation(emptyUrl));
        assertNull(target.extractLocation(nullUrl));
    }

    @Test
    void shouldExtractCoordinatesUsingGeocodingServiceForQueryUrl() {
        // given
        String urlWithQuery = "https://www.google.com/maps?q=New+York";
        String query = "New York";
        Coordinate expectedCoordinate = new Coordinate(40.7128, -74.0060);
        LocationResult expectedResult = LocationResult.fromCoordinatesAndName(expectedCoordinate, "New York");

        // Configure mocks
        when(mockUrlPatternExtractor.findPlaceId(urlWithQuery)).thenReturn(Optional.empty());
        when(mockUrlPatternExtractor.findAddressQuery(urlWithQuery)).thenReturn(Optional.of(query));
        when(mockHybridGeocodingService.geocodeQuery(query)).thenReturn(expectedResult);

        // when
        LocationResult result = target.extractLocation(urlWithQuery);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(expectedCoordinate.getLat(), result.getCoordinates().getLat());
        assertEquals(expectedCoordinate.getLon(), result.getCoordinates().getLon());
        assertEquals("New York", result.getAddress());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findPlaceId(urlWithQuery);
        verify(mockUrlPatternExtractor).findAddressQuery(urlWithQuery);
        verify(mockHybridGeocodingService).geocodeQuery(query);
    }

    @Test
    void shouldExtractCoordinatesUsingGeocodingServiceForPlaceIdUrl() {
        // given
        String urlWithPlaceId = "https://www.google.com/maps/place/Statue+of+Liberty/data=!4m6!3m5!1s0x89c25090129c363d:0x40c6a5770d25022b";
        String placeId = "0x89c25090129c363d:0x40c6a5770d25022b";
        Coordinate expectedCoordinate = new Coordinate(40.6892494, -74.0445004);
        LocationResult expectedResult = LocationResult.fromCoordinatesAndName(expectedCoordinate, "Statue of Liberty");

        // Configure mocks
        when(mockUrlPatternExtractor.findPlaceId(urlWithPlaceId)).thenReturn(Optional.of(placeId));
        when(mockHybridGeocodingService.getLocationFromPlaceId(placeId)).thenReturn(expectedResult);

        // when
        LocationResult result = target.extractLocation(urlWithPlaceId);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(expectedCoordinate.getLat(), result.getCoordinates().getLat());
        assertEquals(expectedCoordinate.getLon(), result.getCoordinates().getLon());
        assertEquals("Statue of Liberty", result.getAddress());

        // Verify the services were called
        verify(mockUrlPatternExtractor).findPlaceId(urlWithPlaceId);
        verify(mockHybridGeocodingService).getLocationFromPlaceId(placeId);
    }

}
