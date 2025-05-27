package com.example.mapsbridge.config;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for the official Mailtrap Java client.
 */
@Slf4j
@Configuration
public class MailtrapClientConfig {

    @Value("${mailtrap.enabled:false}")
    private boolean mailtrapEnabled;

    @Value("${mailtrap.api.token:}")
    private String apiToken;

    /**
     * Creates a MailtrapClient bean configured with the API token.
     *
     * @return MailtrapClient instance
     */
    @Bean
    @Primary
    public MailtrapClient mailtrapClient() {
        if (!mailtrapEnabled || apiToken.isEmpty()) {
            log.info("Mailtrap client is disabled or API token is not set.");
            return null;
        }

        final MailtrapConfig config = new MailtrapConfig.Builder()
                .token(apiToken)
                .build();

        log.info("Mailtrap client initialized");
        return MailtrapClientFactory.createMailtrapClient(config);
    }
}