package com.nexus.agent.metrics;

import com.nexus.agent.client.NexusApiClient;
import com.nexus.agent.config.AgentManagementProperties;
import org.springframework.stereotype.Component;

@Component
public class RuntimeErrorReporter {
    private final NexusApiClient apiClient;
    private final AgentManagementProperties.Metrics properties;

    public RuntimeErrorReporter(NexusApiClient apiClient, AgentManagementProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties.getMetrics();
    }

    public void report() {
        if (isNotConfigured()) {
            return;
        }
        String token = apiClient.authenticate(new NexusApiClient.Login(properties.getAdminUsername(),
                properties.getAdminPassword())).accessToken();
        apiClient.recordRuntimeError(token);
    }

    private boolean isNotConfigured() {
        return properties.getAdminUsername() == null || properties.getAdminUsername().isBlank()
                || properties.getAdminPassword() == null || properties.getAdminPassword().isBlank();
    }
}