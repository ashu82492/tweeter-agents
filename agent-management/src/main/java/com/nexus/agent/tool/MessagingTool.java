package com.nexus.agent.tool;

import com.nexus.agent.client.NexusApiClient;
import com.nexus.agent.config.AgentManagementProperties;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MessagingTool {
    private final NexusApiClient apiClient;
    private final AgentManagementProperties.Messaging properties;

    public MessagingTool(NexusApiClient apiClient, AgentManagementProperties properties) {
        this.apiClient = apiClient;
        this.properties = properties.getMessaging();
    }

    public List<NexusApiClient.BackendMessage> history(String token, UUID recipientId) {
        NexusApiClient.BackendChat chat = apiClient.createChat(token, recipientId);
        return apiClient.fetchMessages(token, chat.id(), properties.getRecentChatLimit());
    }

    public void send(String token, UUID recipientId, String content, String idempotencyKey) {
        NexusApiClient.BackendChat chat = apiClient.createChat(token, recipientId);
        apiClient.postMessage(token, chat.id(), content, idempotencyKey);
    }
}