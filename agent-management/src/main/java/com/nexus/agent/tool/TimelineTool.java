package com.nexus.agent.tool;

import com.nexus.agent.client.NexusApiClient;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TimelineTool {
    private final NexusApiClient apiClient;

    public TimelineTool(NexusApiClient apiClient) { this.apiClient = apiClient; }

    public List<NexusApiClient.BackendTweet> fetch(String token, int limit) {
        return apiClient.fetchTimeline(token, limit).stream().map(tweetId -> apiClient.fetchTweet(token, tweetId)).toList();
    }
}