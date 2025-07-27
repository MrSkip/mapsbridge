
package com.example.mapsbridge.config.metrics;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;

/**
 * Configuration for CloudWatch Metrics.
 * This class configures both the CloudWatchAsyncClient and CloudWatchMeterRegistry
 * required by micrometer-registry-cloudwatch2 to send metrics to CloudWatch.
 */
@Configuration
public class CloudWatchMetricsConfig {

    @Value("${management.metrics.export.cloudwatch.region:${AWS_REGION:eu-north-1}}")
    private String region;

    @Value("${management.metrics.export.cloudwatch.namespace:mapsbridge}")
    private String namespace;

    @Value("${management.metrics.export.cloudwatch.step:PT1M}")
    private String step;

    @Value("${management.metrics.export.cloudwatch.batch-size:20}")
    private int batchSize;

    @Value("${management.metrics.export.cloudwatch.enabled:true}")
    private boolean enabled;

    /**
     * Creates a CloudWatchAsyncClient bean with the configured region.
     */
    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder()
                .region(Region.of(region))
                .build();
    }

    /**
     * Creates a CloudWatch-specific configuration for the MeterRegistry.
     */
    @Bean
    public CloudWatchConfig cloudWatchConfig() {
        return new CloudWatchConfig() {
            @Override
            public String get(String key) {
                return null; // Accept defaults
            }

            @Override
            public String namespace() {
                return namespace;
            }

            @Override
            public Duration step() {
                return Duration.parse(step);
            }

            @Override
            public int batchSize() {
                return batchSize;
            }

            @Override
            public boolean enabled() {
                return enabled;
            }
        };
    }

    /**
     * Creates the CloudWatch MeterRegistry that will actually send metrics to CloudWatch.
     * This is the primary MeterRegistry that will be used throughout the application.
     */
    @Bean
    @Primary
    public MeterRegistry cloudWatchMeterRegistry(CloudWatchConfig cloudWatchConfig,
                                                 CloudWatchAsyncClient cloudWatchAsyncClient) {
        return new CloudWatchMeterRegistry(cloudWatchConfig, Clock.SYSTEM, cloudWatchAsyncClient);
    }
}