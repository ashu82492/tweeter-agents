package com.think9.app.metrics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ErrorMetricsInterceptor implements HandlerInterceptor {
    private final MetricService metricService;

    public ErrorMetricsInterceptor(MetricService metricService) {
        this.metricService = metricService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        if (response.getStatus() >= 500) {
            metricService.recordError();
        }
    }
}