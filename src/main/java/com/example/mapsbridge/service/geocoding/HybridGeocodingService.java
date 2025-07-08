
package com.example.mapsbridge.service.geocoding;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Hybrid geocoding service that combines Radar.io and Google Maps API.
 * - Uses Radar for reverse geocoding (coordinates to address)
 * - Uses Google for place ID lookups
 * - Uses both services with fallback for address queries
 */
@Service
@Slf4j
public class HybridGeocodingService implements GeocodingService {

    private final GoogleGeocodingServiceImpl googleService;
    private final RadarGeocodingService radarService;
    private final Counter hybridServiceCounter;

    @Autowired
    public HybridGeocodingService(
            GoogleGeocodingServiceImpl googleService,
            RadarGeocodingService radarService,
            Counter.Builder geocodingCounterBuilder,
            MeterRegistry meterRegistry) {

        this.googleService = googleService;
        this.radarService = radarService;

        // Initialize counter
        this.hybridServiceCounter = geocodingCounterBuilder
                .tag("service", "hybrid")
                .register(meterRegistry);
    }

    /**
     * Reverse geocodes coordinates to get a location name.
     * Primarily uses Radar.io for this operation with Google as fallback.
     */
    @Override
//    @Cacheable("reverse-geocode")
    public LocationResult reverseGeocode(Coordinate coordinate) {
        if (coordinate == null || !coordinate.isValid()) {
            return LocationResult.fromCoordinates(coordinate);
        }

        hybridServiceCounter.increment();
        log.info("Reverse geocoding coordinates: {},{}", coordinate.getLat(), coordinate.getLon());

        return tryRadarReverseGeocode(coordinate)
                .orElseGet(() -> tryGoogleReverseGeocode(coordinate)
                        .orElse(LocationResult.fromCoordinates(coordinate)));
    }

    /**
     * Gets location information from a place ID.
     * Only Google supports place IDs, so this always uses Google Maps API.
     */
    @Override
//    @Cacheable("place-id-lookup")
    public LocationResult getLocationFromPlaceId(String placeId) {
        if (StringUtils.isBlank(placeId)) {
            return null;
        }

        hybridServiceCounter.increment();
        log.debug("Looking up place ID: {}", placeId);

        if (!googleService.isEnabled()) {
            log.warn("Google Maps API is not enabled, cannot lookup place ID: {}", placeId);
            return null;
        }

        return tryGooglePlaceIdLookup(placeId)
                .map(this::enrichWithRadarLocationName)
                .orElse(null);
    }

    /**
     * Forward geocodes a query to get coordinates and location name.
     * Tries Radar first, then falls back to Google.
     */
    @Override
//    @Cacheable("forward-geocode")
    public LocationResult geocodeQuery(String query) {
        if (StringUtils.isBlank(query)) {
            return null;
        }

        hybridServiceCounter.increment();
        log.debug("Forward geocoding query: {}", query);

        return tryRadarForwardGeocode(query)
                .orElseGet(() -> tryGoogleForwardGeocode(query)
                        .orElse(null));
    }

    @Override
    public boolean isEnabled() {
        return radarService.isEnabled() || googleService.isEnabled();
    }

    // Private helper methods

    private Optional<LocationResult> tryRadarReverseGeocode(Coordinate coordinate) {
        if (!radarService.isEnabled()) {
            return Optional.empty();
        }

        LocationResult result = radarService.reverseGeocode(coordinate);
        if (isValidLocationResult(result)) {
            log.debug("Successfully reverse geocoded with Radar.io: {}", result.getAddress());
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Optional<LocationResult> tryGoogleReverseGeocode(Coordinate coordinate) {
        if (!googleService.isEnabled()) {
            return Optional.empty();
        }

        LocationResult result = googleService.reverseGeocode(coordinate);
        if (isValidLocationResult(result)) {
            log.debug("Successfully reverse geocoded with Google: {}", result.getAddress());
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Optional<LocationResult> tryGooglePlaceIdLookup(String placeId) {
        LocationResult result = googleService.getLocationFromPlaceId(placeId);
        return (result != null && result.hasValidCoordinates()) ? Optional.of(result) : Optional.empty();
    }

    private LocationResult enrichWithRadarLocationName(LocationResult googleResult) {
        if (googleResult.getAddress() != null || !radarService.isEnabled()) {
            return googleResult;
        }

        return tryRadarReverseGeocode(googleResult.getCoordinates())
                .map(radarResult -> LocationResult.fromCoordinatesAndName(
                        googleResult.getCoordinates(), radarResult.getAddress()))
                .orElse(googleResult);
    }

    private Optional<LocationResult> tryRadarForwardGeocode(String query) {
        if (!radarService.isEnabled()) {
            return Optional.empty();
        }

        LocationResult result = radarService.geocodeQuery(query);
        if (result != null && result.hasValidCoordinates()) {
            logForwardGeocodeSuccess("Radar.io", result);
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private Optional<LocationResult> tryGoogleForwardGeocode(String query) {
        if (!googleService.isEnabled()) {
            return Optional.empty();
        }

        LocationResult result = googleService.geocodeQuery(query);
        if (result != null && result.hasValidCoordinates()) {
            logForwardGeocodeSuccess("Google", result);
            return Optional.of(result);
        }
        return Optional.empty();
    }

    private boolean isValidLocationResult(LocationResult result) {
        return result != null && result.getAddress() != null;
    }

    private void logForwardGeocodeSuccess(String service, LocationResult result) {
        log.info("Successfully forward geocoded with {}: {},{} - {}",
                service,
                result.getCoordinates().getLat(),
                result.getCoordinates().getLon(),
                result.getAddress());
    }
}