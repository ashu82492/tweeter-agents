package com.nexus.agent.client;

import com.nexus.agent.config.AgentManagementProperties;
import java.util.List;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestNexusApiClient implements NexusApiClient {
    private static final ParameterizedTypeReference<List<UUID>> UUID_LIST = new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<BackendMessage>> MESSAGE_LIST = new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    @Autowired
    public RestNexusApiClient(RestClient.Builder restClientBuilder, AgentManagementProperties properties) {
        this(restClientBuilder.baseUrl(properties.getApiBaseUrl()).build());
    }

    RestNexusApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public BackendUser register(Registration request) {
        return restClient.post().uri("/auth/register").contentType(MediaType.APPLICATION_JSON).body(request)
                .retrieve().body(BackendUser.class);
    }

    @Override
    public Token authenticate(Login request) {
        return restClient.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON).body(request)
                .retrieve().body(Token.class);
    }

    @Override
    public BackendTweet postTweet(String token, String content, String idempotencyKey) {
        return restClient.post().uri("/tweets").headers(headers -> authorize(headers, token)).header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON).body(new Content(content)).retrieve().body(BackendTweet.class);
    }

    @Override
    public List<UUID> fetchTimeline(String token, int limit) {
        List<UUID> result = restClient.get().uri(uriBuilder -> uriBuilder.path("/timeline/feed")
                .queryParam("limit", limit).build()).headers(headers -> authorize(headers, token)).retrieve().body(UUID_LIST);
        return result == null ? List.of() : result;
    }

    @Override
    public BackendTweet fetchTweet(String token, UUID tweetId) {
        return restClient.get().uri("/tweets/{tweetId}", tweetId).headers(headers -> authorize(headers, token))
                .retrieve().body(BackendTweet.class);
    }

    @Override
    public FollowResult follow(String token, UUID userId, String idempotencyKey) {
        return restClient.post().uri("/users/{userId}/follow", userId).headers(headers -> authorize(headers, token))
                .header("Idempotency-Key", idempotencyKey)
                .retrieve().body(FollowResult.class);
    }

    @Override
    public BackendChat createChat(String token, UUID participantId) {
        return restClient.post().uri("/chats").headers(headers -> authorize(headers, token)).contentType(MediaType.APPLICATION_JSON)
                .body(new ChatRequest(participantId)).retrieve().body(BackendChat.class);
    }

    @Override
    public List<BackendMessage> fetchMessages(String token, UUID chatId, int limit) {
        List<BackendMessage> result = restClient.get().uri(uriBuilder -> uriBuilder.path("/chats/{chatId}/messages")
                .queryParam("limit", limit).build(chatId)).headers(headers -> authorize(headers, token)).retrieve().body(MESSAGE_LIST);
        return result == null ? List.of() : result;
    }

    @Override
    public BackendMessage postMessage(String token, UUID chatId, String content, String idempotencyKey) {
        return restClient.post().uri("/chats/{chatId}/messages", chatId).headers(headers -> authorize(headers, token))
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON).body(new Content(content)).retrieve().body(BackendMessage.class);
    }

    @Override
    public void recordRuntimeError(String adminToken) {
        restClient.post().uri("/admin/metrics/errors").headers(headers -> authorize(headers, adminToken)).retrieve().toBodilessEntity();
    }

    private void authorize(HttpHeaders headers, String token) {
        headers.setBearerAuth(token);
    }

    private record Content(String content) { }
    private record ChatRequest(UUID participantId) { }
}