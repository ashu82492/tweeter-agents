package com.think9.app.metrics;

public record MetricsSnapshot(
        long agents,
        long activeAgents,
        long tweetsPerMinute,
        long dmsPerMinute,
        long errors,
        String health
) {
}