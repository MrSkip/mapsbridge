package com.example.mapsbridge.provider;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.exception.InvalidCoordinateException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
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

    /**
     * Pattern to extract coordinates from URLs.
     * Should have capturing groups for latitude and longitude.
     */
    protected Pattern coordinatePattern;

    /**
     * Constructor with OkHttpClient injection.
     * 
     * @param httpClient The OkHttpClient to use for HTTP requests
     * @param urlTemplate The URL template for generating map links
     * @param urlPattern The pattern to match URLs from this provider
     * @param coordinatePattern The pattern to extract coordinates from URLs
     */
    public AbstractMapProvider(OkHttpClient httpClient, String urlTemplate, Pattern urlPattern, Pattern coordinatePattern) {
        this.httpClient = httpClient;
        this.urlTemplate = urlTemplate;
        this.urlPattern = urlPattern;
        this.coordinatePattern = coordinatePattern;
    }

    @Override
    public String generateUrl(Coordinate coordinate) {
        if (coordinate == null || !coordinate.isValid()) {
            throw new InvalidCoordinateException("Invalid coordinates");
        }

        return urlTemplate
                .replace("{lat}", String.valueOf(coordinate.getLat()))
                .replace("{lon}", String.valueOf(coordinate.getLon()));
    }

    @Override
    public boolean isProviderUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        boolean matches = urlPattern.matcher(url).matches();
        if (matches) {
            log.info("URL provider is {}", getType());
        }
        return matches;
    }

    @Override
    public LocationResult extractLocation(String url) {
        if (StringUtils.isBlank(url)) {
            return new LocationResult();
        }

        // First, try to extract coordinates from the URL directly
        LocationResult result = this.parseLocation(url);
        if (result != null) {
            return result;
        }

        // If not found, follow redirects and try again
        String finalUrl = followRedirects(url);
        if (!url.equals(finalUrl)) {
            return this.parseLocation(finalUrl);
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

    /**
     * Follow redirects to get the final URL.
     * 
     * @param shortUrl The initial URL
     * @return The final URL after following redirects, or the original URL if no redirects
     */
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
}
