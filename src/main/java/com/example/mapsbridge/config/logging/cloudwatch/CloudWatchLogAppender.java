package com.example.mapsbridge.config.logging.cloudwatch;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import lombok.Setter;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CloudWatchLogAppender extends AppenderBase<ILoggingEvent> {

    private final AtomicReference<String> sequenceToken = new AtomicReference<>();
    private final LinkedBlockingQueue<InputLogEvent> logEvents = new LinkedBlockingQueue<>();

    @Setter
    private volatile String logGroupName;
    @Setter
    private volatile String logStreamName;
    @Setter
    private volatile String region;

    private volatile CloudWatchLogsClient cloudWatchLogsClient;
    private volatile ScheduledExecutorService scheduler;

    @Setter
    private volatile Layout<ILoggingEvent> layout;
    @Setter
    private volatile Encoder<ILoggingEvent> encoder;

    @Override
    public void start() {
        if (logGroupName == null || logStreamName == null || region == null) {
            addError("CloudWatch log configuration is incomplete");
            return;
        }

        try {
            cloudWatchLogsClient = CloudWatchLogsClient.builder()
                    .region(Region.of(region))
                    .build();

            initializeLogGroup();
            initializeLogStream();

            // Create and start scheduler
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "CloudWatchLogAppender-" + getName());
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleAtFixedRate(this::sendBatch, 5, 5, TimeUnit.SECONDS);

            super.start();
        } catch (Exception e) {
            addError("Failed to start CloudWatch log appender", e);
            cleanup();
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) return;

        try {
            String message;
            if (encoder != null) {
                message = new String(encoder.encode(event));
            } else if (layout != null) {
                message = layout.doLayout(event);
            } else {
                message = event.getFormattedMessage();
            }

            InputLogEvent logEvent = InputLogEvent.builder()
                    .timestamp(event.getTimeStamp())
                    .message(message.trim())
                    .build();

            // If queue is full, remove oldest event to prevent memory issues
            if (!logEvents.offer(logEvent)) {
                logEvents.poll(); // Remove oldest
                logEvents.offer(logEvent); // Add new
            }
        } catch (Exception e) {
            addError("Failed to append log event", e);
        }
    }

    private void sendBatch() {
        List<InputLogEvent> batch = new ArrayList<>();
        logEvents.drainTo(batch, 10000); // CloudWatch limit

        if (batch.isEmpty()) return;

        try {
            PutLogEventsRequest.Builder requestBuilder = PutLogEventsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .logEvents(batch);

            String currentToken = sequenceToken.get();
            if (currentToken != null) {
                requestBuilder.sequenceToken(currentToken);
            }

            PutLogEventsResponse response = cloudWatchLogsClient.putLogEvents(requestBuilder.build());
            sequenceToken.set(response.nextSequenceToken());

        } catch (InvalidSequenceTokenException e) {
            // Reset sequence token and retry once
            sequenceToken.set(null);
            addWarn("Invalid sequence token, retrying without token", e);
            retryBatch(batch);
        } catch (Exception e) {
            addError("Failed to send logs to CloudWatch", e);
            // Re-queue failed events (up to a limit to prevent memory issues)
            if (logEvents.size() < 50000) {
                batch.forEach(logEvents::offer);
            }
        }
    }

    private void retryBatch(List<InputLogEvent> batch) {
        try {
            PutLogEventsRequest request = PutLogEventsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .logEvents(batch)
                    .build();

            PutLogEventsResponse response = cloudWatchLogsClient.putLogEvents(request);
            sequenceToken.set(response.nextSequenceToken());
        } catch (Exception e) {
            addError("Failed to retry sending logs to CloudWatch", e);
        }
    }

    private void initializeLogGroup() {
        try {
            cloudWatchLogsClient.createLogGroup(CreateLogGroupRequest.builder()
                    .logGroupName(logGroupName)
                    .build());
        } catch (ResourceAlreadyExistsException e) {
            // Log group already exists, ignore
        } catch (Exception e) {
            addError("Failed to create log group", e);
            throw e;
        }
    }

    private void initializeLogStream() {
        try {
            cloudWatchLogsClient.createLogStream(CreateLogStreamRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .build());
        } catch (ResourceAlreadyExistsException e) {
            // Log stream already exists, ignore
        } catch (Exception e) {
            addError("Failed to create log stream", e);
            throw e;
        }
    }

    @Override
    public void stop() {
        try {
            // Send remaining logs
            sendBatch();
        } catch (Exception e) {
            addError("Error sending final batch of logs", e);
        } finally {
            cleanup();
            super.stop();
        }
    }

    private void cleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (cloudWatchLogsClient != null) {
            try {
                cloudWatchLogsClient.close();
            } catch (Exception e) {
                addError("Error closing CloudWatch client", e);
            }
        }
    }
}