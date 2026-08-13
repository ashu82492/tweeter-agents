package com.think9.agent.tool;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.think9.agent.client.Think9ApiClient;
import com.think9.agent.config.AgentManagementProperties;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MessagingToolTest {
    @Test
    void historyFetchesFiveRecentMessagesByDefault() {
        Think9ApiClient apiClient = Mockito.mock(Think9ApiClient.class);
        UUID recipientId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        when(apiClient.createChat("token", recipientId)).thenReturn(new Think9ApiClient.BackendChat(chatId));
        when(apiClient.fetchMessages("token", chatId, 5)).thenReturn(List.of());

        MessagingTool tool = new MessagingTool(apiClient, new AgentManagementProperties());

        tool.history("token", recipientId);

        verify(apiClient).fetchMessages("token", chatId, 5);
    }

    @Test
    void historyUsesConfiguredMessageLimit() {
        Think9ApiClient apiClient = Mockito.mock(Think9ApiClient.class);
        AgentManagementProperties properties = new AgentManagementProperties();
        properties.getMessaging().setRecentChatLimit(3);
        UUID recipientId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        when(apiClient.createChat("token", recipientId)).thenReturn(new Think9ApiClient.BackendChat(chatId));
        when(apiClient.fetchMessages("token", chatId, 3)).thenReturn(List.of());

        MessagingTool tool = new MessagingTool(apiClient, properties);

        tool.history("token", recipientId);

        verify(apiClient).fetchMessages("token", chatId, 3);
    }
}