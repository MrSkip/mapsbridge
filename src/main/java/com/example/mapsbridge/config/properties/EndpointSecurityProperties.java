package com.example.mapsbridge.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for endpoint security.
 */
@Data
@Configuration
@PropertySource("classpath:endpoint-security.properties")
@PropertySource(value = "classpath:endpoint-security-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
public class EndpointSecurityProperties {

    @Value("${public.endpoints}")
    private String publicEndpoints;

    @Value("${master.only.endpoints}")
    private String masterOnlyEndpoints;

    @Value("${authenticated.endpoints}")
    private String authenticatedEndpoints;

    /**
     * Get public endpoints as a list.
     *
     * @return List of public endpoint patterns
     */
    public List<String> getPublicEndpointsList() {
        return splitToList(publicEndpoints);
    }

    /**
     * Get master-only endpoints as a list.
     *
     * @return List of master-only endpoint patterns
     */
    public List<String> getMasterEndpointsList() {
        return splitToList(masterOnlyEndpoints);
    }

    /**
     * Get authenticated endpoints as a list.
     *
     * @return List of authenticated endpoint patterns
     */
    public List<String> getAuthenticatedEndpointsList() {
        return splitToList(authenticatedEndpoints);
    }

    private List<String> splitToList(String commaSeparatedString) {
        if (commaSeparatedString == null || commaSeparatedString.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(commaSeparatedString.split(","));
    }
}
