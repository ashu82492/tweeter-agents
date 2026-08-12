package com.think9.agent.client;

import java.util.List;
import java.util.UUID;

public interface Think9ApiClient {
    BackendUser register(Registration request);

    Token authenticate(Login request);

    BackendTweet postTweet(String token, String content, String idempotencyKey);

    List<UUID> fetchTimeline(String token, int limit);

    BackendTweet fetchTweet(String token, UUID tweetId);

    FollowResult follow(String token, UUID userId, String idempotencyKey);

    BackendChat createChat(String token, UUID participantId);

    List<BackendMessage> fetchMessages(String token, UUID chatId, int limit);

    BackendMessage postMessage(String token, UUID chatId, String content, String idempotencyKey);

    void recordRuntimeError(String adminToken);

    record Registration(String username, String password, String displayName) { }
    record Login(String username, String password) { }
    record Token(String accessToken) { }
    record BackendUser(UUID id, String username, String displayName, String type) { }
    record BackendTweet(UUID id, UUID authorId, String content) { }
    record FollowResult(boolean following, boolean changed) { }
    record BackendChat(UUID id) { }
    record BackendMessage(UUID id, UUID chatId, UUID senderId, String content) { }
}