package com.example.mapsbridge.config.logging;

import lombok.experimental.UtilityClass;

/**
 * Utility class for storing and retrieving logging context information.
 * This class provides thread-local storage for transaction ID and email
 * to be included in logs.
 */
@UtilityClass
public class LoggingContext {

    private static final ThreadLocal<String> TRANSACTION_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> EMAIL = new ThreadLocal<>();

    /**
     * Sets the transaction ID for the current thread.
     *
     * @param transactionId the transaction ID to set
     */
    public static void setTransactionId(String transactionId) {
        TRANSACTION_ID.set(transactionId);
    }

    /**
     * Gets the transaction ID for the current thread.
     *
     * @return the transaction ID, or null if not set
     */
    public static String getTransactionId() {
        return TRANSACTION_ID.get();
    }

    /**
     * Sets the email for the current thread.
     *
     * @param email the email to set
     */
    public static void setEmail(String email) {
        EMAIL.set(email);
    }

    /**
     * Gets the email for the current thread.
     *
     * @return the email, or null if not set
     */
    public static String getEmail() {
        return EMAIL.get();
    }

    /**
     * Clears all context values for the current thread.
     * This should be called at the end of request processing to prevent memory leaks.
     */
    public static void clear() {
        TRANSACTION_ID.remove();
        EMAIL.remove();
    }
}