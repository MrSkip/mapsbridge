package com.example.mapsbridge.config.logging.cloudwatch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StructuredLogEvent {

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("level")
    private String level;

    @JsonProperty("logger")
    private String logger;

    @JsonProperty("thread")
    private String thread;

    @JsonProperty("message")
    private String message;

    @JsonProperty("exception")
    private String exception;

    @JsonProperty("dimensions")
    private LogDimensions dimensions;

    @JsonProperty("custom_fields")
    private Map<String, Object> customFields;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LogDimensions {
        @JsonProperty("transaction_id")
        private String transactionId;

        @JsonProperty("email")
        private String email;

        @JsonProperty("application")
        private String application;

        @JsonProperty("environment")
        private String environment;

        @JsonProperty("instance_id")
        private String instanceId;

        @JsonProperty("service")
        private String service;

        @JsonProperty("version")
        private String version;
    }
}