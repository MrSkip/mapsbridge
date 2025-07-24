package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.GoogleCoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that fetches the URL content and extracts location information from HTML meta tags.
 */
@Component
@Order(1)
@Slf4j
public class G1UrlContentExtractor implements GoogleCoordinateExtractor {

    private static final Pattern[] META_TITLE_PATTERNS = {
            Pattern.compile(
                    "<meta[^>]+content=[\"']([^\"']+)[\"'][^>]*(?:property=[\"']og:title[\"']|itemprop=[\"']name[\"'])",
                    Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile(
                    "<meta[^>]+(?:property=[\"']og:title[\"']|itemprop=[\"']name[\"'])[^>]*content=[\"']([^\"']+)[\"']",
                    Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile(
                    "<meta[^>]*content=[\"']([^\"']*[^\"']*)[\"']",
                    Pattern.CASE_INSENSITIVE
            )
    };

    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            "@([\\-+]?\\d+(?:\\.\\d+)?),([\\-+]?\\d+(?:\\.\\d+)?)"
    );

    private static final Pattern PLACE_ADDRESS_SEPARATOR = Pattern.compile("\\s*[·•]\\s*");

    private final OkHttpClient httpClient;

    public G1UrlContentExtractor(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public @NotNull LocationResult extract(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        try {
            return fetchUrlContent(url)
                    .map(content -> extractLocationFromHtml(content, url))
                    .orElse(new LocationResult());
        } catch (Exception e) {
            log.warn("Failed to extract location from URL content: {}", url, e);
            return new LocationResult();
        }
    }

    private Optional<String> fetchUrlContent(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
//                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
//                .header("Accept", "*/*")
//                .header("Accept-Language", "en-US,en;q=0.5")
//                .header("Accept-Encoding", "gzip, deflate")
//                .header("Connection", "keep-alive")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("HTTP request failed with status: {} for URL: {}", response.code(), url);
                return Optional.empty();
            }

            return Optional.ofNullable(response.body())
                    .map(body -> {
                        try {
                            String content = body.string();
                            log.debug("Successfully fetched content from URL: {} (length: {})", url, content.length());
                            return content;
                        } catch (IOException e) {
                            log.warn("Failed to read response body for URL: {}", url, e);
                            return null;
                        }
                    });
        }
    }

    private LocationResult extractLocationFromHtml(String htmlContent, String originalUrl) {
        LocationInfo locationInfo = extractLocationInfo(htmlContent);
        Coordinate coordinates = extractCoordinates(htmlContent, originalUrl);

        LocationResult result = new LocationResult(coordinates, locationInfo.address(), locationInfo.placeName());
        log.info("G1UrlContentExtractor result: coordinates={}, address='{}', placeName='{}'",
                coordinates, locationInfo.address(), locationInfo.placeName());

        return result;
    }

    private LocationInfo extractLocationInfo(String htmlContent) {
        return extractMetaTitle(htmlContent)
                .map(this::parseLocationFromTitle)
                .orElse(LocationInfo.empty());
    }

    private Optional<String> extractMetaTitle(String htmlContent) {
        return Arrays.stream(META_TITLE_PATTERNS)
                .map(pattern -> extractWithPattern(htmlContent, pattern))
                .filter(StringUtils::isNotBlank)
                .map(this::decode)
                .findFirst();
    }

    private LocationInfo parseLocationFromTitle(String title) {
        String[] parts = PLACE_ADDRESS_SEPARATOR.split(title, 2);

        if (parts.length == 2) {
            String placeName = parts[0].trim();
            String address = parts[1].trim();
            log.debug("Split title - Place: '{}', Address: '{}'", placeName, address);
            return new LocationInfo(placeName, address);
        } else {
            String placeName = title.trim();
            log.debug("Using full title as place name: '{}'", placeName);
            return new LocationInfo(placeName, null);
        }
    }

    @Nullable
    private String extractWithPattern(String htmlContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(htmlContent);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (StringUtils.isNotBlank(group)) {
                    return group;
                }
            }
        }
        return null;
    }

    @Nullable
    private Coordinate extractCoordinates(String htmlContent, String originalUrl) {
        return extractCoordinatesFromContent(htmlContent)
                .or(() -> extractCoordinatesFromUrl(originalUrl))
                .orElse(null);
    }

    private Optional<Coordinate> extractCoordinatesFromContent(String htmlContent) {
        return extractCoordinatesFromText(htmlContent, "HTML content");
    }

    private Optional<Coordinate> extractCoordinatesFromUrl(String url) {
        return extractCoordinatesFromText(url, "original URL");
    }

    private Optional<Coordinate> extractCoordinatesFromText(String text, String source) {
        Matcher matcher = COORDINATE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                double lat = Double.parseDouble(matcher.group(1));
                double lon = Double.parseDouble(matcher.group(2));
                Coordinate coordinate = new Coordinate(lat, lon);

                if (coordinate.isValid()) {
                    log.debug("Extracted coordinates from {}: {},{}", source, lat, lon);
                    return Optional.of(coordinate);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid coordinate format found in {}", source);
            }
        }
        return Optional.empty();
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
     * Record to hold location information parsed from title
     */
    private record LocationInfo(String placeName, String address) {
        static LocationInfo empty() {
            return new LocationInfo(null, null);
        }
    }
}