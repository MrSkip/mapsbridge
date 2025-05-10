package com.example.mapsbridge.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Slf4j
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(MapsBridgeTelegramBot telegramBot) throws TelegramApiException {
        log.info("Initializing Telegram Bots API");
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(telegramBot);
        log.info("Telegram bot registered successfully");
        return api;
    }
}
