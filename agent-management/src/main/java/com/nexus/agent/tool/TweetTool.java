package com.nexus.agent.tool;

import com.nexus.agent.client.NexusApiClient;
import org.springframework.stereotype.Component;

@Component
public class TweetTool {
    private final NexusApiClient apiClient;

    public TweetTool(NexusApiClient apiClient) { this.apiClient = apiClient; }

    public void post(String token, String content, String idempotencyKey) {
        apiClient.postTweet(token, content, idempotencyKey);
    }
}