package com.example.mapsbridge.config.logging.cloudwatch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.encoder.EncoderBase;
import com.example.mapsbridge.config.logging.LoggingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class JsonLogEncoder extends EncoderBase<ILoggingEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Setter
    private String applicationName = "mapsbridge";

    @Setter
    private String environment = "production";

    @Setter
    private String version = "1.0.0";

    @Setter
    private boolean includeLocationInfo = false;

    private String instanceId;

    @Override
    public void start() {
        try {
            // Get instance ID (hostname or IP)
            instanceId = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            instanceId = "unknown";
        }
        super.start();
    }

    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        try {
            StructuredLogEvent logEvent = createStructuredLogEvent(event);
            String json = OBJECT_MAPPER.writeValueAsString(logEvent);
            return (json + System.lineSeparator()).getBytes();
        } catch (Exception e) {
            // Fallback to simple format if JSON encoding fails
            String fallback = String.format("%s [%s] %-5s %s - %s%n",
                    Instant.ofEpochMilli(event.getTimeStamp()),
                    event.getThreadName(),
                    event.getLevel(),
                    event.getLoggerName(),
                    event.getFormattedMessage());
            return fallback.getBytes();
        }
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }

    private StructuredLogEvent createStructuredLogEvent(ILoggingEvent event) {
        // Create dimensions
        StructuredLogEvent.LogDimensions.LogDimensionsBuilder dimensionsBuilder =
                StructuredLogEvent.LogDimensions.builder()
                        .application(applicationName)
                        .environment(environment)
                        .instanceId(instanceId)
                        .service(applicationName)
                        .version(version);

        // Add context-specific dimensions
        String transactionId = LoggingContext.getTransactionId();
        if (StringUtils.hasText(transactionId)) {
            dimensionsBuilder.transactionId(transactionId);
        }

        String email = LoggingContext.getEmail();
        if (StringUtils.hasText(email)) {
            dimensionsBuilder.email(email);
        }

        // Create custom fields for additional context
        Map<String, Object> customFields = new HashMap<>();

        // Add location information if enabled
        if (includeLocationInfo && event.hasCallerData()) {
            StackTraceElement[] callerData = event.getCallerData();
            if (callerData.length > 0) {
                StackTraceElement caller = callerData[0];
                customFields.put("class", caller.getClassName());
                customFields.put("method", caller.getMethodName());
                customFields.put("line", caller.getLineNumber());
                customFields.put("file", caller.getFileName());
            }
        }

        // Add MDC properties if available
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        if (mdcPropertyMap != null && !mdcPropertyMap.isEmpty()) {
            customFields.put("mdc", mdcPropertyMap);
        }

        // Build the structured log event
        StructuredLogEvent.StructuredLogEventBuilder builder = StructuredLogEvent.builder()
                .timestamp(Instant.ofEpochMilli(event.getTimeStamp()))
                .level(event.getLevel().toString())
                .logger(event.getLoggerName())
                .thread(event.getThreadName())
                .message(event.getFormattedMessage())
                .dimensions(dimensionsBuilder.build());

        // Add exception information if present
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            builder.exception(formatException(throwableProxy));
        }

        // Add custom fields if any
        if (!customFields.isEmpty()) {
            builder.customFields(customFields);
        }

        return builder.build();
    }

    private String formatException(IThrowableProxy throwableProxy) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // Format the exception with stack trace
        pw.println(throwableProxy.getClassName() + ": " + throwableProxy.getMessage());

        StackTraceElementProxy[] stackTrace = throwableProxy.getStackTraceElementProxyArray();
        if (stackTrace != null) {
            for (StackTraceElementProxy element : stackTrace) {
                pw.println("\tat " + element.getStackTraceElement().toString());
            }
        }

        // Include cause if present
        IThrowableProxy cause = throwableProxy.getCause();
        if (cause != null) {
            pw.println("Caused by: " + formatException(cause));
        }

        return sw.toString().trim();
    }
}