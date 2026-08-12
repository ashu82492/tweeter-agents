package com.think9.app.metrics;

import com.think9.core.events.MessageCreated;
import com.think9.core.events.TweetPublished;
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