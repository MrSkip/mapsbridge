package com.example.mapsbridge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.mapsbridge.security.ApiKeyAuthFilter;
import com.example.mapsbridge.security.ApiKeyAuthToken;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${api.security.token}")
    private String apiToken;

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    @Value("${cors.allowed-methods}")
    private String corsAllowedMethods;

    @Value("${cors.allowed-headers}")
    private String corsAllowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean corsAllowCredentials;

    private final Environment environment;

    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Define API endpoints that are always public
        String[] alwaysPublicPaths = {
            "/api/convert",
            "/actuator/health"
        };

        // Determine which paths should be public based on active profiles
        List<String> publicPaths = new ArrayList<>(Arrays.asList(alwaysPublicPaths));

        whitelistEndpointsBasedOnEnvProfile(publicPaths);

        // Convert List<String> to String[] for use with ApiKeyAuthFilter and requestMatchers
        String[] publicPathsArray = publicPaths.toArray(new String[0]);

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(publicPathsArray);
        filter.setAuthenticationManager(authenticationManager());

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                corsConfiguration.setAllowedOrigins(Arrays.asList(corsAllowedOrigins.split(",")));
                corsConfiguration.setAllowedMethods(Arrays.asList(corsAllowedMethods.split(",")));
                corsConfiguration.setAllowedHeaders(Arrays.asList(corsAllowedHeaders.split(",")));
                corsConfiguration.setAllowCredentials(corsAllowCredentials);
                return corsConfiguration;
            }))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(publicPathsArray).permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    private void whitelistEndpointsBasedOnEnvProfile(List<String> publicPaths) {
        // Define Swagger UI endpoints that are only public in dev/local environments
        String[] swaggerPaths = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**"
        };

        // Check if current profile is dev or local
        boolean isDevOrLocal = Arrays.stream(environment.getActiveProfiles())
            .anyMatch(profile -> profile.equals("dev") || profile.equals("local"));

        // If dev or local profile is active, add Swagger UI paths to public paths
        if (isDevOrLocal) {
            publicPaths.addAll(Arrays.asList(swaggerPaths));
        }
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> {
            ApiKeyAuthToken auth = (ApiKeyAuthToken) authentication;
            String principal = (String) auth.getPrincipal();

            if (!apiToken.equals(principal)) {
                auth.setAuthenticated(false);
                return auth;
            }

            auth.setAuthenticated(true);
            return auth;
        };
    }
}
