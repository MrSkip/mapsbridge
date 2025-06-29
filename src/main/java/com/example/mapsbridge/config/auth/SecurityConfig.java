package com.example.mapsbridge.config.auth;

import com.example.mapsbridge.config.auth.security.ApiKeyAuthFilter;
import com.example.mapsbridge.config.auth.security.ApiKeyAuthToken;
import com.example.mapsbridge.config.properties.CorsProperties;
import com.example.mapsbridge.config.properties.EndpointSecurityProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private static final String AUTHENTICATION_FAILED_MESSAGE = "Authentication failed";
    private static final String ACCESS_DENIED_MESSAGE_FORMAT = "Access denied: %s";

    private final AuthenticationManager authenticationManager;
    private final CorsProperties corsProperties;
    private final EndpointSecurityProperties endpointSecurityProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(this::configureSessionManagement)
                .addFilterBefore(createApiKeyAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(this::configureAuthorization)
                .exceptionHandling(this::configureExceptionHandling)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> createCorsConfiguration();
    }

    private void configureSessionManagement(
            org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer<HttpSecurity> session) {
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private void configureAuthorization(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {

        String[] publicEndpoints = getEndpointsArray(endpointSecurityProperties.getPublicEndpointsList());
        String[] masterEndpoints = getEndpointsArray(endpointSecurityProperties.getMasterEndpointsList());
        String[] authenticatedEndpoints = getEndpointsArray(endpointSecurityProperties.getAuthenticatedEndpointsList());

        authorize
                .requestMatchers(publicEndpoints).permitAll()
                .requestMatchers(masterEndpoints).hasAuthority(ApiKeyAuthToken.ROLE_MASTER)
                .requestMatchers(authenticatedEndpoints)
                .hasAnyAuthority(ApiKeyAuthToken.ROLE_MASTER, ApiKeyAuthToken.ROLE_API_USER)
                .anyRequest().authenticated();
    }

    private void configureExceptionHandling(
            org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer<HttpSecurity> exceptionHandling) {
        exceptionHandling
                .authenticationEntryPoint(this::handleAuthenticationException)
                .accessDeniedHandler(this::handleAccessDeniedException);
    }

    private void handleAuthenticationException(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.security.core.AuthenticationException authException) throws IOException {
        log.warn("Authentication failed for request {}: {}", request.getRequestURI(), authException.getMessage());
        writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, AUTHENTICATION_FAILED_MESSAGE);
    }

    private void handleAccessDeniedException(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied for request {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
        String errorMessage = String.format(ACCESS_DENIED_MESSAGE_FORMAT, accessDeniedException.getMessage());
        writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, errorMessage);
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\":\"%s\"}", message));
    }

    private CorsConfiguration createCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOriginsList());
        configuration.setAllowedMethods(corsProperties.getAllowedMethodsList());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeadersList());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        return configuration;
    }

    private ApiKeyAuthFilter createApiKeyAuthFilter() {
        String[] publicEndpoints = getEndpointsArray(endpointSecurityProperties.getPublicEndpointsList());
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(publicEndpoints);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    private String[] getEndpointsArray(java.util.List<String> endpointsList) {
        return endpointsList.toArray(new String[0]);
    }
}