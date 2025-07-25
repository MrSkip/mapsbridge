package com.example.mapsbridge.provider.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class HttpClientUtils {
    private final OkHttpClient httpClient;

    /**
     * Fetches content from a URL using HTTP GET request.
     *
     * @param url The URL to fetch content from
     * @return Optional containing the content as a string, or empty if the request failed
     * @throws IOException If an I/O error occurs during the request
     */
    public Optional<String> fetchUrlContent(String url) throws IOException {
        if (StringUtils.isBlank(url)) {
            return Optional.empty();
        }

        Request request = new Request.Builder().url(url).build();

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
}
