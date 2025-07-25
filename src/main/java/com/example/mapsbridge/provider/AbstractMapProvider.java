package com.example.mapsbridge.provider;

import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.exception.InvalidCoordinateException;
import com.example.mapsbridge.provider.extractor.CoordinateExtractor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Abstract base class for map providers with common functionality.
 */
@Slf4j
public abstract class AbstractMapProvider implements MapProvider {

    protected OkHttpClient httpClient;

    /**
     * The URL template for generating map links.
     * Should contain placeholders for latitude and longitude.
     */
    protected String urlTemplate;

    /**
     * Pattern to match URLs from this provider.
     */
    protected Pattern urlPattern;

    protected List<? extends CoordinateExtractor> extractors;

    /**
     * Constructor with OkHttpClient injection.
     *
     * @param httpClient  The OkHttpClient to use for HTTP requests
     * @param urlTemplate The URL template for generating map links
     * @param urlPattern  The pattern to match URLs from this provider
     */
    public AbstractMapProvider(OkHttpClient httpClient,
                               String urlTemplate,
                               Pattern urlPattern,
                               List<? extends CoordinateExtractor> extractors) {
        this.httpClient = httpClient;
        this.urlTemplate = urlTemplate;
        this.urlPattern = urlPattern;
        this.extractors = extractors;
    }

    @Override
    public String generateUrl(LocationResult location) {
        if (location == null || !location.hasValidCoordinates()) {
            throw new InvalidCoordinateException("Invalid coordinates");
        }

        return urlTemplate
                .replace("{lat}", String.valueOf(location.getCoordinates().getLat()))
                .replace("{lon}", String.valueOf(location.getCoordinates().getLon()));
    }

    @Override
    public boolean isProviderUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        return urlPattern.matcher(url).matches();
    }

    @Override
    public LocationResult extractLocation(String url) {
        if (!isProviderUrl(url)) {
            return new LocationResult();
        }

        log.info("URL provider is {}", getType());

        if (StringUtils.isBlank(url)) {
            return null;
        }

        String finalUrl = followRedirects(url);

        // Apply each extractor in the chain until one returns a non-null result
        for (CoordinateExtractor extractor : extractors) {
            LocationResult locationResult = extractor.extract(finalUrl);
            if (locationResult.hasValidCoordinates()) {
                locationResult.setMapSource(getType());
                String extractorName = extractor.getClass().getSimpleName();
                log.info("Extracted location using {}: {}", extractorName, locationResult);
                return locationResult;
            }
        }

        return new LocationResult();
    }

    /**
     * Follow redirects to get the final URL.
     *
     * @param shortUrl The initial URL
     * @return The final URL after following redirects, or the original URL if no redirects
     */
    protected String followRedirects(String shortUrl) {
        try {
            // Create a request with explicit redirect handling
            Request request = new Request.Builder()
                    .url(shortUrl)
                    .header("User-Agent", "Mozilla/5.0 (compatible; MapsBot/1.0)")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String finalUrl = response.request().url().toString();
                    log.debug("Followed redirects from {} to {}", shortUrl, finalUrl);
                    return finalUrl;
                } else {
                    log.warn("HTTP request failed with code {} for URL: {}", response.code(), shortUrl);
                    return shortUrl;
                }
            }
        } catch (Exception e) {
            log.error("Error following redirects for URL {}: {}", shortUrl, e.getMessage(), e);
            return shortUrl;
        }
    }

    /**
     * Validates the location result and throws an exception if invalid.
     */
    protected void validateLocation(LocationResult location) {
        if (location == null || !location.hasValidCoordinates()) {
            throw new InvalidCoordinateException("Invalid coordinates provided");
        }
    }
}
