package com.example.mapsbridge.config.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

/**
 * Configuration for CloudWatch Metrics.
 * This class explicitly configures the CloudWatchAsyncClient bean required by
 * micrometer-registry-cloudwatch2 to send metrics to CloudWatch.
 */
@Configuration
public class CloudWatchMetricsConfig {

    @Value("${management.metrics.export.cloudwatch.region:${AWS_REGION:eu-north-1}}")
    private String region;

    /**
     * Creates a CloudWatchAsyncClient bean with the configured region.
     * This client is used by micrometer-registry-cloudwatch2 to send metrics to CloudWatch.
     *
     * @return CloudWatchAsyncClient configured with the specified region
     */
    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder()
                .region(Region.of(region))
                .build();
    }
}