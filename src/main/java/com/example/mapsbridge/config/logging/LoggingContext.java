package com.example.mapsbridge.config.logging;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

/**
 * Utility class for storing and retrieving logging context information.
 * This class uses MDC (Mapped Diagnostic Context) to store transaction ID, email,
 * chat ID, and IP address to be included in logs.
 */
@UtilityClass
public class LoggingContext {

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String EMAIL_KEY = "email";
    private static final String CHAT_ID_KEY = "chatId";
    private static final String IP_ADDRESS_KEY = "ipAddress";

    /**
     * Gets the transaction ID for the current thread.
     *
     * @return the transaction ID, or null if not set
     */
    public static String getTransactionId() {
        return MDC.get(TRANSACTION_ID_KEY);
    }

    /**
     * Sets the transaction ID for the current thread.
     *
     * @param transactionId the transaction ID to set
     */
    public static void setTransactionId(String transactionId) {
        MDC.put(TRANSACTION_ID_KEY, transactionId);
    }

    /**
     * Gets the email for the current thread.
     *
     * @return the email, or null if not set
     */
    public static String getEmail() {
        return MDC.get(EMAIL_KEY);
    }

    /**
     * Sets the email for the current thread.
     *
     * @param email the email to set
     */
    public static void setEmail(String email) {
        MDC.put(EMAIL_KEY, email);
    }

    /**
     * Gets the chat ID for the current thread.
     *
     * @return the chat ID, or null if not set
     */
    public static String getChatId() {
        return MDC.get(CHAT_ID_KEY);
    }

    /**
     * Sets the chat ID for the current thread.
     *
     * @param chatId the chat ID to set
     */
    public static void setChatId(String chatId) {
        MDC.put(CHAT_ID_KEY, chatId);
    }

    /**
     * Gets the IP address for the current thread.
     *
     * @return the IP address, or null if not set
     */
    public static String getIpAddress() {
        return MDC.get(IP_ADDRESS_KEY);
    }

    /**
     * Sets the IP address for the current thread.
     *
     * @param ipAddress the IP address to set
     */
    public static void setIpAddress(String ipAddress) {
        MDC.put(IP_ADDRESS_KEY, ipAddress);
    }

    /**
     * Clears all context values for the current thread.
     * This should be called at the end of request processing to prevent memory leaks.
     */
    public static void clear() {
        MDC.remove(TRANSACTION_ID_KEY);
        MDC.remove(EMAIL_KEY);
        MDC.remove(CHAT_ID_KEY);
        MDC.remove(IP_ADDRESS_KEY);
    }
}
