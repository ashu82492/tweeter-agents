package com.nexus.app.web;

import com.nexus.app.metrics.MetricService;
import com.nexus.app.metrics.MetricsSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminMetricsController {
    private final MetricService metricService;

    public AdminMetricsController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping("/metrics")
    MetricsSnapshot metrics() {
        return metricService.snapshot();
    }

    @PostMapping("/metrics/errors")
    void recordError() {
        metricService.recordError();
    }
}