package com.example.mapsbridge.telegram;

import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.telegram.service.ResponseFormatterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class MapsBridgeTelegramBot extends TelegramLongPollingBot {

    private static final String ERROR_MESSAGE_TEMPLATE = "Sorry, I couldn't process your message";
    private final ResponseFormatterService responseFormatterService;
    @Value("${telegram.bot.username}")
    private String botUsername;

    public MapsBridgeTelegramBot(
            @Value("${telegram.bot.token}") String token,
            ResponseFormatterService responseFormatterService) {
        super(token);
        this.responseFormatterService = responseFormatterService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!hasTextMessage(update)) {
            log.info("Ignoring update with no text message: {}", update);
            return;
        }

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        String chatIdStr = String.valueOf(chatId);

        processMessageWithLoggingContext(messageText, chatId, chatIdStr);
    }

    private void processMessageWithLoggingContext(String messageText, long chatId, String chatIdStr) {
        LoggingContext.setChatId(chatIdStr);
        try {
            log.info("Received message: '{}' from chat ID: {}", messageText, chatId);
            processMessage(messageText, chatIdStr);
            log.info("Map links response sent to chat ID: {}", chatId);
        } catch (Exception e) {
            handleProcessingError(e, chatId, chatIdStr);
        } finally {
            LoggingContext.clear();
        }
    }

    private void processMessage(String messageText, String chatIdStr) throws TelegramApiException {
        String responseText = responseFormatterService.convertMessageToMapLinks(messageText);
        SendMessage message = createResponseMessage(chatIdStr, responseText);
        execute(message);
    }

    private void handleProcessingError(Exception e, long chatId, String chatIdStr) {
        log.error("Error processing message from chat ID: {}", chatId, e);
        try {
            sendErrorMessage(e, chatIdStr);
        } catch (TelegramApiException ex) {
            log.error("Failed to send error message to chat ID: {}", chatId, ex);
        }
    }

    private SendMessage createResponseMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    private void sendErrorMessage(Exception e, String chatId) throws TelegramApiException {
        SendMessage errorMessage = createResponseMessage(chatId, ERROR_MESSAGE_TEMPLATE);
        execute(errorMessage);
    }

    private boolean hasTextMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }
}