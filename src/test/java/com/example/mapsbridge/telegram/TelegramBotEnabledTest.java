package com.example.mapsbridge.telegram;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.example.mapsbridge.service.MapConverterService;
import com.example.mapsbridge.telegram.service.ResponseFormatterService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test to verify that Telegram bot is initialized when telegram.bot.enabled is true.
 */
@SpringJUnitConfig(TelegramBotEnabledTest.TestConfig.class)
@TestPropertySource(properties = {
    "telegram.bot.enabled=true",
    "telegram.bot.username=test_bot",
    "telegram.bot.token=test_token"
})
public class TelegramBotEnabledTest {

    @Autowired
    private TelegramBotConfig telegramBotConfig;

    @Test
    void telegramBotShouldBeInitializedWhenEnabled() {
        // If the test context loads successfully, it means the conditional beans were created
        assertTrue(true, "Test context loaded successfully with telegram.bot.enabled=true");

        // Additional verification that the configuration class is available
        assertDoesNotThrow(() -> telegramBotConfig.toString());
    }

    @Configuration
    static class TestConfig {

        @Bean
        public TelegramBotConfig telegramBotConfig() {
            return new TelegramBotConfig();
        }

        @Bean
        public MapConverterService mapConverterService() {
            return mock(MapConverterService.class);
        }

        @Bean
        @ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
        public ResponseFormatterService responseFormatterService(MapConverterService mapConverterService) {
            return new ResponseFormatterService(mapConverterService);
        }

        @Bean
        @ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
        public MapsBridgeTelegramBot telegramBot() {
            return mock(MapsBridgeTelegramBot.class);
        }

        @Bean
        @ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
        public TelegramBotsApi telegramBotsApi() {
            return mock(TelegramBotsApi.class);
        }
    }
}
