package com.example.mapsbridge.provider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import com.example.mapsbridge.exception.InvalidCoordinateException;
import com.example.mapsbridge.model.Coordinate;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base class for map providers with common functionality.
 */
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

        return urlPattern.matcher(url).matches();
    }

    @Override
    public Coordinate extractCoordinates(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        // First, try to extract coordinates from the URL directly
        Coordinate coordinate = this.parseCoordinates(url);
        if (coordinate != null) {
            return coordinate;
        }

        // If not found, follow redirects and try again
        String finalUrl = followRedirects(url);
        if (!url.equals(finalUrl)) {
            return this.parseCoordinates(finalUrl);
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
            System.out.println("No coordinates found in: " + url);
        }

        return null;
    }

    /**
     * Follow redirects to get the final URL.
     * 
     * @param shortUrl The initial URL
     * @return The final URL after following redirects, or the original URL if no redirects
     */
    protected String followRedirects(String shortUrl) {
        Request request = new Request.Builder()
                .url(shortUrl)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.request().url().toString();
            }
        } catch (Exception e) {
            return shortUrl;
        }
        return shortUrl;
    }
}
