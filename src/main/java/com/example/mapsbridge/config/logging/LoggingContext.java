package com.example.mapsbridge.config.logging;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

/**
 * Utility class for storing and retrieving logging context information.
 * This class uses MDC (Mapped Diagnostic Context) to store transaction ID and email
 * to be included in logs.
 */
@UtilityClass
public class LoggingContext {

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String EMAIL_KEY = "email";

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
     * Clears all context values for the current thread.
     * This should be called at the end of request processing to prevent memory leaks.
     */
    public static void clear() {
        MDC.remove(TRANSACTION_ID_KEY);
        MDC.remove(EMAIL_KEY);
    }
}