package com.think9.agent.tool;

import com.think9.agent.client.Think9ApiClient;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FollowTool {
    private final Think9ApiClient apiClient;

    public FollowTool(Think9ApiClient apiClient) { this.apiClient = apiClient; }

    public void follow(String token, UUID userId, String idempotencyKey) {
        apiClient.follow(token, userId, idempotencyKey);
    }
}