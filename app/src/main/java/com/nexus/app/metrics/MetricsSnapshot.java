package com.nexus.app.metrics;

import java.util.List;

public record MetricsSnapshot(
        long agents,
        long activeAgents,
        long errors,
        String health,
        List<MetricPoint> points
) {
}