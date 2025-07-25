package com.example.mapsbridge.provider.extractor;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that fetches the URL content and extracts location information from Apple Maps HTML.
 */
@Slf4j
@AllArgsConstructor
public abstract class DefaultExtractor implements CoordinateExtractor {
    protected final OkHttpClient httpClient;
    protected final Pattern coordinatePattern;

    @NotNull
    public LocationResult extract(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        // First, try to extract coordinates from the URL directly
        LocationResult result = this.parseLocation(url);
        if (result != null && result.hasValidCoordinates()) {
            return result;
        }

        return new LocationResult();
    }

    @Nullable
    private LocationResult parseLocation(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        Coordinate coordinate = parseCoordinates(url);
        if (coordinate != null) {
            // For now, we don't have a way to extract location name, so we set it to null
            return LocationResult.fromCoordinates(coordinate);
        }

        return null;
    }

    @Nullable
    private Coordinate parseCoordinates(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        Matcher matcher = coordinatePattern.matcher(url);
        if (matcher.find()) {
            String latStr = matcher.group("lat") != null ? matcher.group("lat") : matcher.group("lat2");
            String lonStr = matcher.group("lon") != null ? matcher.group("lon") : matcher.group("lon2");

            // Normalize European decimal commas to dots
            latStr = latStr.replace(',', '.');
            lonStr = lonStr.replace(',', '.');

            try {
                double latitude = Double.parseDouble(latStr);
                double longitude = Double.parseDouble(lonStr);

                return new Coordinate(latitude, longitude);
            } catch (NumberFormatException e) {
                System.out.println("Invalid coordinate format in: " + url);
            }
        } else {
            log.info("No coordinates found for URL: {}", url);
        }

        return null;
    }
}