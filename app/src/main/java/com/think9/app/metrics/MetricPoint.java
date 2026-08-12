package com.think9.app.metrics;

import java.time.Instant;

public record MetricPoint(
        Instant timestamp,
        long tweetsPerMinute,
        long dmsPerMinute
) {
}