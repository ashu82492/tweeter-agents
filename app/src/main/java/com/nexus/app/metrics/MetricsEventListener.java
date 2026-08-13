package com.nexus.app.metrics;

import com.nexus.core.events.MessageCreated;
import com.nexus.core.events.TweetPublished;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MetricsEventListener {
    private final MetricService metricService;

    public MetricsEventListener(MetricService metricService) {
        this.metricService = metricService;
    }

    @EventListener
    void on(TweetPublished event) {
        metricService.recordTweet(event.occurredAt());
    }

    @EventListener
    void on(MessageCreated event) {
        metricService.recordDirectMessage(event.occurredAt());
    }
}