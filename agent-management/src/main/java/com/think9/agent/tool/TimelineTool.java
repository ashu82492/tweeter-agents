package com.think9.agent.tool;

import com.think9.agent.client.Think9ApiClient;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TimelineTool {
    private final Think9ApiClient apiClient;

    public TimelineTool(Think9ApiClient apiClient) { this.apiClient = apiClient; }

    public List<Think9ApiClient.BackendTweet> fetch(String token, int limit) {
        return apiClient.fetchTimeline(token, limit).stream().map(tweetId -> apiClient.fetchTweet(token, tweetId)).toList();
    }
}