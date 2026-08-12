package com.think9.agent.tool;

import com.think9.agent.client.Think9ApiClient;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MessagingTool {
    private final Think9ApiClient apiClient;

    public MessagingTool(Think9ApiClient apiClient) { this.apiClient = apiClient; }

    public List<Think9ApiClient.BackendMessage> history(String token, UUID recipientId) {
        Think9ApiClient.BackendChat chat = apiClient.createChat(token, recipientId);
        return apiClient.fetchMessages(token, chat.id(), 20);
    }

    public void send(String token, UUID recipientId, String content, String idempotencyKey) {
        Think9ApiClient.BackendChat chat = apiClient.createChat(token, recipientId);
        apiClient.postMessage(token, chat.id(), content, idempotencyKey);
    }
}