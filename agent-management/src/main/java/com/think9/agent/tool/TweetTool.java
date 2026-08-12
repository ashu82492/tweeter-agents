package com.think9.agent.tool;

import com.think9.agent.client.Think9ApiClient;
import org.springframework.stereotype.Component;

@Component
public class TweetTool {
    private final Think9ApiClient apiClient;

    public TweetTool(Think9ApiClient apiClient) { this.apiClient = apiClient; }

    public void post(String token, String content, String idempotencyKey) {
        apiClient.postTweet(token, content, idempotencyKey);
    }
}