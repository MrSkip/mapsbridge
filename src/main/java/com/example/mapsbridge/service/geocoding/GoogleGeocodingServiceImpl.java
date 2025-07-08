
package com.example.mapsbridge.service.geocoding;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Google implementation of the GeocodingService.
 * Provides geocoding services using Google Maps API for place ID lookups,
 * reverse geocoding, and address queries.
 */
@Slf4j
@Service
public class GoogleGeocodingServiceImpl implements GeocodingService {

    private final GeoApiContext geoApiContext;
    private final boolean googleApiEnabled;
    private final GoogleApiCounters counters;

    @Autowired
    public GoogleGeocodingServiceImpl(
            GeoApiContext geoApiContext,
            @Value("${google.api.enabled:false}") boolean googleApiEnabled,
            Counter.Builder geocodingCounterBuilder,
            MeterRegistry meterRegistry) {

        this.geoApiContext = geoApiContext;
        this.googleApiEnabled = googleApiEnabled;
        this.counters = new GoogleApiCounters(geocodingCounterBuilder, meterRegistry);
    }

    @Override
    public LocationResult reverseGeocode(Coordinate coordinate) {
        if (!isValidRequest(coordinate)) {
            return LocationResult.fromCoordinates(coordinate);
        }

        try {
            GeocodingResult[] results = performReverseGeocoding(coordinate);
            counters.incrementReverseGeocode();

            return processReverseGeocodingResults(results, coordinate);
        } catch (Exception e) {
            log.warn("Error during reverse geocoding for coordinate {}: {}", coordinate, e.getMessage());
            return LocationResult.fromCoordinates(coordinate);
        }
    }

    @Override
    public LocationResult getLocationFromPlaceId(String placeId) {
        if (!isValidPlaceIdRequest(placeId)) {
            return null;
        }

        try {
            PlaceDetails placeDetails = fetchPlaceDetails(placeId);
            counters.incrementPlaceIdLookup();

            return processPlaceDetails(placeDetails);
        } catch (Exception e) {
            log.error("Error getting location from place ID {}: {}", placeId, e.getMessage());
            return handlePlaceIdFallback(placeId);
        }
    }

    @Override
    public LocationResult geocodeQuery(String query) {
        if (!isValidGeocodingQuery(query)) {
            return null;
        }

        try {
            GeocodingResult[] results = performForwardGeocoding(query);
            counters.incrementForwardGeocode();

            return processGeocodingResults(results);
        } catch (Exception e) {
            log.warn("Error during forward geocoding for query '{}': {}", query, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isEnabled() {
        return googleApiEnabled;
    }

    /**
     * Gets coordinates for a place ID using minimal API calls.
     * This is a cost-optimized operation that only fetches geometry data.
     *
     * @param placeId The place ID to lookup
     * @return Coordinate object or null if not found
     */
    public Coordinate getPlaceCoordinates(String placeId) {
        if (!isValidPlaceIdRequest(placeId)) {
            return null;
        }

        try {
            PlaceDetails placeDetails = fetchPlaceDetailsWithGeometryOnly(placeId);
            counters.incrementPlaceIdLookup();

            return extractCoordinatesFromPlaceDetails(placeDetails);
        } catch (Exception e) {
            log.error("Error getting coordinates for place ID {}: {}", placeId, e.getMessage());
            return null;
        }
    }

    // Private validation methods
    private boolean isValidRequest(Coordinate coordinate) {
        return isEnabled() && coordinate != null && coordinate.isValid();
    }

    private boolean isValidPlaceIdRequest(String placeId) {
        return isEnabled() && StringUtils.isNotBlank(placeId);
    }

    private boolean isValidGeocodingQuery(String query) {
        return isEnabled() && StringUtils.isNotBlank(query);
    }

    // Private API interaction methods
    private GeocodingResult[] performReverseGeocoding(Coordinate coordinate) throws Exception {
        LatLng latLng = new LatLng(coordinate.getLat(), coordinate.getLon());
        return GeocodingApi.reverseGeocode(geoApiContext, latLng).await();
    }

    private GeocodingResult[] performForwardGeocoding(String query) throws Exception {
        return GeocodingApi.geocode(geoApiContext, query).await();
    }

    private PlaceDetails fetchPlaceDetails(String placeId) throws Exception {
        return PlacesApi.placeDetails(geoApiContext, placeId).await();
    }

    private PlaceDetails fetchPlaceDetailsWithGeometryOnly(String placeId) throws Exception {
        return PlacesApi.placeDetails(geoApiContext, placeId)
                .fields(PlaceDetailsRequest.FieldMask.GEOMETRY)
                .await();
    }

    // Private result processing methods
    private LocationResult processReverseGeocodingResults(GeocodingResult[] results, Coordinate coordinate) {
        if (results != null && results.length > 0) {
            String locationName = results[0].formattedAddress;
            return LocationResult.fromCoordinatesAndName(coordinate, locationName);
        }
        return LocationResult.fromCoordinates(coordinate);
    }

    private LocationResult processPlaceDetails(PlaceDetails placeDetails) {
        if (hasValidGeometry(placeDetails)) {
            Coordinate coordinate = extractCoordinatesFromPlaceDetails(placeDetails);
            String address = placeDetails.formattedAddress;
            String placeName = placeDetails.name;
            return new LocationResult(coordinate, address, placeName);
        }
        return null;
    }

    private LocationResult processGeocodingResults(GeocodingResult[] results) {
        if (results != null && results.length > 0) {
            GeocodingResult result = results[0];
            Coordinate coordinate = extractCoordinatesFromGeocodingResult(result);
            String locationName = result.formattedAddress;
            return LocationResult.fromCoordinatesAndName(coordinate, locationName);
        }
        return null;
    }

    private LocationResult handlePlaceIdFallback(String placeId) {
        try {
            Coordinate coordinate = getPlaceCoordinates(placeId);
            return coordinate != null ? LocationResult.fromCoordinates(coordinate) : null;
        } catch (Exception ex) {
            log.debug("Place ID fallback also failed for {}: {}", placeId, ex.getMessage());
            return null;
        }
    }

    // Private utility methods
    private boolean hasValidGeometry(PlaceDetails placeDetails) {
        return placeDetails != null &&
                placeDetails.geometry != null &&
                placeDetails.geometry.location != null;
    }

    private Coordinate extractCoordinatesFromPlaceDetails(PlaceDetails placeDetails) {
        if (hasValidGeometry(placeDetails)) {
            double lat = placeDetails.geometry.location.lat;
            double lng = placeDetails.geometry.location.lng;
            return new Coordinate(lat, lng);
        }
        return null;
    }

    private Coordinate extractCoordinatesFromGeocodingResult(GeocodingResult result) {
        double lat = result.geometry.location.lat;
        double lng = result.geometry.location.lng;
        return new Coordinate(lat, lng);
    }

    /**
     * Inner class to handle metrics counters for Google API operations.
     * Encapsulates counter creation and increment operations.
     */
    private static class GoogleApiCounters {
        private final Counter placeIdLookupCounter;
        private final Counter reverseGeocodeCounter;
        private final Counter forwardGeocodeCounter;

        public GoogleApiCounters(Counter.Builder geocodingCounterBuilder, MeterRegistry meterRegistry) {
            this.placeIdLookupCounter = geocodingCounterBuilder
                    .tag("service", "google")
                    .tag("operation", "placeIdLookup")
                    .register(meterRegistry);

            this.reverseGeocodeCounter = geocodingCounterBuilder
                    .tag("service", "google")
                    .tag("operation", "reverseGeocode")
                    .register(meterRegistry);

            this.forwardGeocodeCounter = geocodingCounterBuilder
                    .tag("service", "google")
                    .tag("operation", "forwardGeocode")
                    .register(meterRegistry);
        }

        public void incrementPlaceIdLookup() {
            placeIdLookupCounter.increment();
        }

        public void incrementReverseGeocode() {
            reverseGeocodeCounter.increment();
        }

        public void incrementForwardGeocode() {
            forwardGeocodeCounter.increment();
        }
    }
}