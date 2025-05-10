package com.example.mapsbridge.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.mapsbridge.telegram.service.ResponseFormatterService;

@Component
public class MapsBridgeTelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MapsBridgeTelegramBot.class);

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final ResponseFormatterService responseFormatterService;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public MapsBridgeTelegramBot(
            @Value("${telegram.bot.token}") String token,
            ResponseFormatterService responseFormatterService) {
        super(token);
        this.responseFormatterService = responseFormatterService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            logger.info("Received message: '{}' from chat ID: {}", messageText, chatId);

            try {
                // Convert the message to map links
                String responseText = responseFormatterService.convertMessageToMapLinks(messageText);

                // Create and configure the response message
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(responseText);
                message.setParseMode(ParseMode.HTML); // Enable HTML formatting for clickable links

                // Send the response
                execute(message);
                logger.info("Map links response sent to chat ID: {}", chatId);
            } catch (Exception e) {
                logger.error("Error processing message from chat ID: {}", chatId, e);

                // Send error message
                try {
                    sendErrorMessage(e, chatId);
                } catch (TelegramApiException ex) {
                    logger.error("Failed to send error message to chat ID: {}", chatId, ex);
                }
            }
        }
    }

    private void sendErrorMessage(Exception e, long chatId) throws TelegramApiException {
        SendMessage errorMessage = new SendMessage();
        errorMessage.setChatId(String.valueOf(chatId));
        errorMessage.setText("Sorry, I couldn't process your message: " + e.getMessage());
        execute(errorMessage);
    }
}
