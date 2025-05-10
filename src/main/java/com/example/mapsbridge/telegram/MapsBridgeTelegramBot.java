package com.example.mapsbridge.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MapsBridgeTelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MapsBridgeTelegramBot.class);

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public MapsBridgeTelegramBot(@Value("${telegram.bot.token}") String token) {
        super(token);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            
            logger.info("Received message: '{}' from chat ID: {}", messageText, chatId);
            
            // Simple echo response for now
            String responseText = "You said: " + messageText;
            
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(responseText);
            
            try {
                execute(message);
                logger.info("Response sent to chat ID: {}", chatId);
            } catch (TelegramApiException e) {
                logger.error("Failed to send message to chat ID: {}", chatId, e);
            }
        }
    }
}