package com.example.mapsbridge.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for CORS settings.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    private String allowedOrigins;
    private String allowedMethods;
    private String allowedHeaders;
    private boolean allowCredentials;

    public List<String> getAllowedOriginsList() {
        return Arrays.asList(allowedOrigins.split(","));
    }

    public List<String> getAllowedMethodsList() {
        return Arrays.asList(allowedMethods.split(","));
    }

    public List<String> getAllowedHeadersList() {
        return Arrays.asList(allowedHeaders.split(","));
    }

}
