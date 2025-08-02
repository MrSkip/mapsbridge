package com.example.mapsbridge.provider.extractor.apple;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.utils.HttpClientUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that fetches the URL content and extracts location information from Apple Maps HTML.
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class A1UrlContentExtractor implements AppleCoordinateExtractor {

    private static final Pattern PLACE_NAME_PATTERN = Pattern.compile(
            "<meta[^>]+property=[\"']og:title[\"'][^>]*content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LATITUDE_PATTERN = Pattern.compile(
            "<meta[^>]+property=[\"']place:location:latitude[\"'][^>]*content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LONGITUDE_PATTERN = Pattern.compile(
            "<meta[^>]+property=[\"']place:location:longitude[\"'][^>]*content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "<title>([^<]+)</title>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SHORT_ADDRESS_PATTERN = Pattern.compile(
            "\"shortAddress\":\\s*\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );

    private final HttpClientUtils httpClientUtils;

    @Override
    public @NotNull LocationResult extract(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        try {
            return httpClientUtils.fetchUrlContent(url)
                    .map(content -> extractLocationFromHtml(content, url))
                    .orElse(new LocationResult());
        } catch (Exception e) {
            log.warn("Failed to extract location from Apple Maps URL content: {}", url, e);
            return new LocationResult();
        }
    }

    private LocationResult extractLocationFromHtml(String htmlContent, String originalUrl) {
        LocationInfo locationInfo = extractLocationInfo(htmlContent);
        Coordinate coordinates = extractCoordinates(htmlContent);

        // Don't use "Marked Location" as place name
        String placeName = "Marked Location".equals(locationInfo.placeName()) ? null : locationInfo.placeName();

        LocationResult result = new LocationResult(null, null, coordinates, locationInfo.address(), placeName);
        log.info("A1UrlContentExtractor result: coordinates={}, address='{}', placeName='{}'",
                coordinates, locationInfo.address(), placeName);

        return result;
    }

    private LocationInfo extractLocationInfo(String htmlContent) {
        String placeName = extractPlaceName(htmlContent);
        String address = extractAddress(htmlContent);
        return new LocationInfo(placeName, address);
    }

    @Nullable
    private String extractPlaceName(String htmlContent) {
        return extractWithPattern(htmlContent, PLACE_NAME_PATTERN);
    }

    @Nullable
    private String extractAddress(String htmlContent) {
        // First try to extract from JSON data
        return Optional.ofNullable(extractWithPattern(htmlContent, SHORT_ADDRESS_PATTERN))
                .filter(StringUtils::isNotBlank)
                .or(() -> extractAddressFromTitle(htmlContent))
                .orElse(null);
    }

    private Optional<String> extractAddressFromTitle(String htmlContent) {
        return Optional.ofNullable(extractWithPattern(htmlContent, TITLE_PATTERN))
                .filter(StringUtils::isNotBlank)
                .map(this::parseAddressFromTitle);
    }

    @Nullable
    private String parseAddressFromTitle(String title) {
        int inIndex = title.indexOf(" in ");
        int dashIndex = title.lastIndexOf(" - ");

        if (inIndex > 0 && dashIndex > inIndex) {
            return title.substring(inIndex + 4, dashIndex);
        }

        return null;
    }

    @Nullable
    private Coordinate extractCoordinates(String htmlContent) {
        String latStr = extractWithPattern(htmlContent, LATITUDE_PATTERN);
        String lonStr = extractWithPattern(htmlContent, LONGITUDE_PATTERN);

        if (StringUtils.isNotBlank(latStr) && StringUtils.isNotBlank(lonStr)) {
            return parseCoordinateStrings(latStr, lonStr);
        }

        return null;
    }

    @Nullable
    private Coordinate parseCoordinateStrings(String latStr, String lonStr) {
        try {
            double lat = Double.parseDouble(latStr);
            double lon = Double.parseDouble(lonStr);
            Coordinate coordinate = new Coordinate(lat, lon);

            if (coordinate.isValid()) {
                log.debug("Extracted coordinates from HTML content: {},{}", lat, lon);
                return coordinate;
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid coordinate format found in HTML content: lat='{}', lon='{}'", latStr, lonStr);
        }

        return null;
    }

    @Nullable
    private String extractWithPattern(String htmlContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(htmlContent);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (StringUtils.isNotBlank(group)) {
                    return decode(group);
                }
            }
        }
        return null;
    }

    String decode(String content) {
        if (content == null) return null;

        return content.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ");
    }

    /**
     * Record to hold location information
     */
    private record LocationInfo(String placeName, String address) {
    }
}