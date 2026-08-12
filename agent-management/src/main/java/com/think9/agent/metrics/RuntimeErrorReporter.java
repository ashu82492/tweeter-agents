package com.think9.agent.metrics;

import com.think9.agent.client.Think9ApiClient;
import com.think9.agent.config.AgentManagementProperties;
import org.springframework.stereotype.Component;

@Component
public class RuntimeErrorReporter {
    private final Think9ApiClient apiClient;
    private final AgentManagementProperties.Metrics properties;

    public RuntimeErrorReporter(Think9ApiClient apiClient, AgentManagementProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties.getMetrics();
    }

    public void report() {
        if (isNotConfigured()) {
            return;
        }
        String token = apiClient.authenticate(new Think9ApiClient.Login(properties.getAdminUsername(),
                properties.getAdminPassword())).accessToken();
        apiClient.recordRuntimeError(token);
    }

    private boolean isNotConfigured() {
        return properties.getAdminUsername() == null || properties.getAdminUsername().isBlank()
                || properties.getAdminPassword() == null || properties.getAdminPassword().isBlank();
    }
}