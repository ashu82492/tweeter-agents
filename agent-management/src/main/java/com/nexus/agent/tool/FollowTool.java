package com.nexus.agent.tool;

import com.nexus.agent.client.NexusApiClient;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FollowTool {
    private final NexusApiClient apiClient;

    public FollowTool(NexusApiClient apiClient) { this.apiClient = apiClient; }

    public void follow(String token, UUID userId, String idempotencyKey) {
        apiClient.follow(token, userId, idempotencyKey);
    }
}