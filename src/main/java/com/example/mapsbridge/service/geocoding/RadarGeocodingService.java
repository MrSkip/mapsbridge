package com.example.mapsbridge.service.geocoding;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Optional;

/**
 * Radar.io implementation of the GeocodingService.
 * Primarily used for reverse geocoding (coordinates to address).
 * <p>
 * This service provides:
 * - Reverse geocoding (coordinates to address)
 * - Forward geocoding (address to coordinates)
 * - Metrics collection for monitoring
 * - Proper error handling and fallback behavior
 */
@Service
@Slf4j
public class RadarGeocodingService implements GeocodingService {

    // Configuration constants
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);
    private static final String RADAR_API_BASE_URL = "https://api.radar.io/v1";
    private static final String REVERSE_GEOCODE_ENDPOINT = "/geocode/reverse";
    private static final String FORWARD_GEOCODE_ENDPOINT = "/geocode/forward";

    // JSON field names
    private static final String ADDRESSES_FIELD = "addresses";
    private static final String LATITUDE_FIELD = "latitude";
    private static final String LONGITUDE_FIELD = "longitude";
    private static final String FORMATTED_ADDRESS_FIELD = "formattedAddress";

    private final WebClient webClient;
    private final boolean radarApiEnabled;
    private final Counter reverseGeocodeCounter;
    private final Counter forwardGeocodeCounter;
    private final ObjectMapper objectMapper;

    @Autowired
    public RadarGeocodingService(
            @Value("${radar.api.key:}") String radarApiKey,
            @Value("${radar.api.enabled:false}") boolean radarApiEnabled,
            Counter.Builder geocodingCounterBuilder,
            MeterRegistry meterRegistry) {

        this.radarApiEnabled = radarApiEnabled;
        this.objectMapper = new ObjectMapper();

        // Create WebClient with proper configuration
        this.webClient = createWebClient(radarApiKey);

        // Initialize counters with proper tags
        this.reverseGeocodeCounter = createCounter(geocodingCounterBuilder, "reverseGeocode", meterRegistry);
        this.forwardGeocodeCounter = createCounter(geocodingCounterBuilder, "forwardGeocode", meterRegistry);
    }

    @Override
    public LocationResult reverseGeocode(Coordinate coordinate) {
        if (!isValidRequest(coordinate)) {
            log.debug("Radar API disabled or invalid coordinates: {}", coordinate);
            return LocationResult.fromCoordinates(coordinate);
        }

        try {
            String responseBody = performReverseGeocodeRequest(coordinate);
            reverseGeocodeCounter.increment();
            return parseReverseGeocodeResponse(coordinate, responseBody);
        } catch (Exception e) {
            log.error("Error reverse geocoding with Radar API for coordinates {},{}: {}",
                    coordinate.getLat(), coordinate.getLon(), e.getMessage());
            return LocationResult.fromCoordinates(coordinate);
        }
    }

    @Override
    public LocationResult getLocationFromPlaceId(String placeId) {
        // Radar API doesn't support Google Place IDs
        log.debug("Place ID lookup not supported by Radar API: {}", placeId);
        return null;
    }

    @Override
    public LocationResult geocodeQuery(String query) {
        if (!isValidQuery(query)) {
            log.debug("Radar API disabled or invalid query: {}", query);
            return null;
        }

        try {
            String responseBody = performForwardGeocodeRequest(query);
            forwardGeocodeCounter.increment();
            return parseForwardGeocodeResponse(responseBody);
        } catch (Exception e) {
            log.error("Error forward geocoding with Radar API for query '{}': {}", query, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isEnabled() {
        return radarApiEnabled;
    }

    // Private helper methods

    /**
     * Creates a properly configured WebClient for Radar API requests.
     */
    private WebClient createWebClient(String apiKey) {
        return WebClient.builder()
                .baseUrl(RADAR_API_BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", apiKey)
                .build();
    }

    /**
     * Creates a counter with proper tags for metrics collection.
     */
    private Counter createCounter(Counter.Builder builder, String operation, MeterRegistry registry) {
        return builder
                .tag("service", "radar")
                .tag("operation", operation)
                .register(registry);
    }

    /**
     * Validates if the request can be processed.
     */
    private boolean isValidRequest(Coordinate coordinate) {
        return isEnabled() && coordinate != null && coordinate.isValid();
    }

    /**
     * Validates if the query can be processed.
     */
    private boolean isValidQuery(String query) {
        return isEnabled() && query != null && !query.trim().isEmpty();
    }

    /**
     * Performs the HTTP request for reverse geocoding.
     */
    private String performReverseGeocodeRequest(Coordinate coordinate) {
        String uriString = UriComponentsBuilder.fromPath(REVERSE_GEOCODE_ENDPOINT)
                .queryParam("coordinates", coordinate.getLat() + "," + coordinate.getLon())
                .build().toUriString();

        log.debug("Sending reverse geocoding request to Radar API: {}", uriString);

        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(REQUEST_TIMEOUT)
                .block();
    }

    /**
     * Performs the HTTP request for forward geocoding.
     */
    private String performForwardGeocodeRequest(String query) {
        String uriString = UriComponentsBuilder.fromPath(FORWARD_GEOCODE_ENDPOINT)
                .queryParam("query", query)
                .build().toUriString();

        log.debug("Sending forward geocoding request to Radar API: {}", uriString);

        return webClient.get()
                .uri(uriString)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(REQUEST_TIMEOUT)
                .block();
    }

    /**
     * Parses the JSON response from a reverse geocoding request.
     */
    private LocationResult parseReverseGeocodeResponse(Coordinate coordinate, String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            Optional<JsonNode> firstAddress = getFirstAddress(root);
            if (firstAddress.isEmpty()) {
                log.debug("No addresses found in Radar API response");
                return LocationResult.fromCoordinates(coordinate);
            }

            String formattedAddress = extractFormattedAddress(firstAddress.get());
            log.debug("Extracted formatted address from Radar API: {}", formattedAddress);

            return LocationResult.fromCoordinatesAndName(coordinate, formattedAddress);
        } catch (Exception e) {
            log.error("Error parsing Radar API reverse geocoding response: {}", e.getMessage());
            return LocationResult.fromCoordinates(coordinate);
        }
    }

    /**
     * Parses the JSON response from a forward geocoding request.
     */
    private LocationResult parseForwardGeocodeResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            Optional<JsonNode> firstAddress = getFirstAddress(root);
            if (firstAddress.isEmpty()) {
                log.debug("No addresses found in Radar API response");
                return null;
            }

            JsonNode address = firstAddress.get();
            Optional<Coordinate> coordinate = extractCoordinate(address);

            if (coordinate.isEmpty()) {
                log.debug("No coordinates found in Radar API response");
                return null;
            }

            String formattedAddress = extractFormattedAddress(address);
            log.debug("Extracted coordinates and formatted address from Radar API: {},{} - {}",
                    coordinate.get().getLat(), coordinate.get().getLon(), formattedAddress);

            return LocationResult.fromCoordinatesAndName(coordinate.get(), formattedAddress);
        } catch (Exception e) {
            log.error("Error parsing Radar API forward geocoding response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets the first address from the JSON response.
     */
    private Optional<JsonNode> getFirstAddress(JsonNode root) {
        JsonNode addresses = root.path(ADDRESSES_FIELD);
        return addresses.isArray() && !addresses.isEmpty()
                ? Optional.of(addresses.get(0))
                : Optional.empty();
    }

    /**
     * Extracts coordinates from an address JSON node.
     */
    private Optional<Coordinate> extractCoordinate(JsonNode address) {
        if (!address.has(LATITUDE_FIELD) || !address.has(LONGITUDE_FIELD)) {
            return Optional.empty();
        }

        try {
            double lat = address.get(LATITUDE_FIELD).asDouble();
            double lon = address.get(LONGITUDE_FIELD).asDouble();
            return Optional.of(new Coordinate(lat, lon));
        } catch (Exception e) {
            log.warn("Invalid coordinate format in Radar API response: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extracts the formatted address from an address JSON node.
     * Uses the formattedAddress field directly from the Radar API response.
     */
    private String extractFormattedAddress(JsonNode address) {
        if (address.has(FORMATTED_ADDRESS_FIELD)) {
            return address.get(FORMATTED_ADDRESS_FIELD).asText();
        }

        // Fallback to empty string if formattedAddress is not available
        log.warn("No formattedAddress field found in Radar API response");
        return "";
    }
}