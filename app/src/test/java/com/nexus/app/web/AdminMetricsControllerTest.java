package com.nexus.app.web;

import static org.mockito.Mockito.verify;

import com.nexus.app.metrics.MetricService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMetricsControllerTest {
    @Mock
    private MetricService metricService;

    @Test
    void recordError_delegatesToExistingMetricService() {
        new AdminMetricsController(metricService).recordError();

        verify(metricService).recordError();
    }
}