package com.think9.agent.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestThink9ApiClientTest {
    private MockRestServiceServer server;
    private RestThink9ApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://think9.test/api/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RestThink9ApiClient(builder.build());
    }

    @Test
    void postTweet_usesAgentTokenAndActionIdempotencyKey() {
        UUID tweetId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        server.expect(once(), requestTo("http://think9.test/api/v1/tweets"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer agent-token"))
                .andExpect(header("Idempotency-Key", "action-123"))
                .andRespond(withSuccess("{\"id\":\"" + tweetId + "\",\"authorId\":\"" + agentId
                        + "\",\"content\":\"hello\"}", MediaType.APPLICATION_JSON));

        Think9ApiClient.BackendTweet tweet = client.postTweet("agent-token", "hello", "action-123");

        assertEquals(tweetId, tweet.id());
        assertEquals(agentId, tweet.authorId());
        server.verify();
    }

    @Test
    void fetchTimeline_usesCurrentAgentsToken() {
        UUID tweetId = UUID.randomUUID();
        server.expect(once(), requestTo("http://think9.test/api/v1/timeline/feed?limit=5"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer agent-token"))
                .andRespond(withSuccess("[\"" + tweetId + "\"]", MediaType.APPLICATION_JSON));

        List<UUID> tweetIds = client.fetchTimeline("agent-token", 5);

        assertEquals(List.of(tweetId), tweetIds);
        server.verify();
    }

    @Test
    void postMessage_usesAgentTokenAndActionIdempotencyKey() {
        UUID chatId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        server.expect(once(), requestTo("http://think9.test/api/v1/chats/" + chatId + "/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer agent-token"))
                .andExpect(header("Idempotency-Key", "action-456"))
                .andRespond(withSuccess("{\"id\":\"" + messageId + "\",\"chatId\":\"" + chatId
                        + "\",\"senderId\":\"" + senderId + "\",\"content\":\"hello\"}", MediaType.APPLICATION_JSON));

        Think9ApiClient.BackendMessage message = client.postMessage("agent-token", chatId, "hello", "action-456");

        assertEquals(messageId, message.id());
        server.verify();
    }
}