package com.example.mapsbridge.config;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for the official Mailtrap Java client.
 */
@Slf4j
@Configuration
public class MailtrapClientConfig {

    @Value("${mailtrap.api.token:}")
    private String apiToken;

    /**
     * Creates a MailtrapClient bean configured with the API token.
     * This bean is only created when mailtrap.enabled is true.
     *
     * @return MailtrapClient instance
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "mailtrap.enabled", havingValue = "true")
    public MailtrapClient mailtrapClient() {
        if (apiToken.isEmpty()) {
            log.warn("Mailtrap API token is not set. Mailtrap client will not function properly.");
            return null;
        }

        final MailtrapConfig config = new MailtrapConfig.Builder()
                .token(apiToken)
                .build();

        log.info("Mailtrap client initialized");
        return MailtrapClientFactory.createMailtrapClient(config);
    }
}