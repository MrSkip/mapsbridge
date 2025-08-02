package com.example.mapsbridge.config.metrics.tracker;

import com.example.mapsbridge.config.metrics.MetricTags;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientTracker {

    private final MeterRegistry meterRegistry;
    private final Counter.Builder requestSourceCounterBuilder;

    /**
     * Constructor with dependency injection.
     *
     * @param meterRegistry The meter registry
     */
    @Autowired
    public ClientTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Create counter builder for tracking request sources
        this.requestSourceCounterBuilder = Counter.builder("maps.converter.request.source")
                .description("Number of map conversion requests by source (API, WEB, TELEGRAM)");
    }

    /**
     * Track the source of a map conversion request.
     *
     * @param source The source of the request (API, WEB, TELEGRAM)
     */
    public void trackRequestSource(String source) {
        requestSourceCounterBuilder
                .tag(MetricTags.CLIENT_SOURCE, source)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Track a request from API (email present).
     */
    public void trackApiRequest() {
        trackRequestSource(MetricTags.API);
    }

    /**
     * Track a request from WEB (IP address present).
     */
    public void trackWebRequest() {
        trackRequestSource(MetricTags.WEB);
    }

    /**
     * Track a request from TELEGRAM (chatId present).
     */
    public void trackTelegramRequest() {
        trackRequestSource(MetricTags.TELEGRAM);
    }

    /**
     * Track a request from SDK.
     */
    public void trackSdkRequest() {
        trackRequestSource(MetricTags.SDK);
    }

    /**
     * Track a request from Shortcut.
     */
    public void trackShortcutRequest() {
        trackRequestSource(MetricTags.SHORTCUT);
    }
}