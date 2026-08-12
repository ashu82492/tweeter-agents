package com.think9.app.metrics;

import java.time.Instant;

public interface MetricService {
    void recordTweet(Instant occurredAt);

    void recordDirectMessage(Instant occurredAt);

    void recordError();

    MetricsSnapshot snapshot();
}