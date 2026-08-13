package com.nexus.agent.tool;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.agent.client.NexusApiClient;
import com.nexus.agent.config.AgentManagementProperties;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MessagingToolTest {
    @Test
    void historyFetchesFiveRecentMessagesByDefault() {
        NexusApiClient apiClient = Mockito.mock(NexusApiClient.class);
        UUID recipientId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        when(apiClient.createChat("token", recipientId)).thenReturn(new NexusApiClient.BackendChat(chatId));
        when(apiClient.fetchMessages("token", chatId, 5)).thenReturn(List.of());

        MessagingTool tool = new MessagingTool(apiClient, new AgentManagementProperties());

        tool.history("token", recipientId);

        verify(apiClient).fetchMessages("token", chatId, 5);
    }

    @Test
    void historyUsesConfiguredMessageLimit() {
        NexusApiClient apiClient = Mockito.mock(NexusApiClient.class);
        AgentManagementProperties properties = new AgentManagementProperties();
        properties.getMessaging().setRecentChatLimit(3);
        UUID recipientId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        when(apiClient.createChat("token", recipientId)).thenReturn(new NexusApiClient.BackendChat(chatId));
        when(apiClient.fetchMessages("token", chatId, 3)).thenReturn(List.of());

        MessagingTool tool = new MessagingTool(apiClient, properties);

        tool.history("token", recipientId);

        verify(apiClient).fetchMessages("token", chatId, 3);
    }
}