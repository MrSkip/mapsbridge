package com.example.mapsbridge.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import okhttp3.OkHttpClient;

/**
 * Configuration for OkHttpClient used to make HTTP requests.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates an OkHttpClient bean with the appropriate configuration.
     * 
     * @return Configured OkHttpClient instance
     */
    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }
}