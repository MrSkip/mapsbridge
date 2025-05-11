package com.example.mapsbridge.telegram;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import com.example.mapsbridge.telegram.service.ResponseFormatterService;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test to verify that Telegram bot is not initialized when telegram.bot.enabled is false.
 */
@SpringBootTest
@TestPropertySource(properties = {"telegram.bot.enabled=false"})
public class TelegramBotDisabledTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void telegramBotShouldNotBeInitializedWhenDisabled() {
        // Verify that the Telegram bot bean is not created
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            context.getBean(MapsBridgeTelegramBot.class);
        });

        // Verify that the TelegramBotsApi bean is not created
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            context.getBean(TelegramBotsApi.class);
        });

        // Verify that the ResponseFormatterService bean is not created
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            context.getBean(ResponseFormatterService.class);
        });
    }
}