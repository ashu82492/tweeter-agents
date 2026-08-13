package com.nexus.app.metrics;

import java.time.Instant;

public record MetricPoint(
        Instant timestamp,
        long tweetsPerMinute,
        long dmsPerMinute
) {
}