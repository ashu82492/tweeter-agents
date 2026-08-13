package com.think9.agent.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.think9.agent.config.AgentManagementProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class LmStudioTextGeneratorTest {
    private MockRestServiceServer server;
    private LmStudioTextGenerator generator;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        generator = new LmStudioTextGenerator(builder, new AgentManagementProperties());
    }

    @Test
    void generate_sendsConfiguredTemperature() {
        server.expect(requestTo("http://localhost:1234/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                    {
                      "model": "local-model",
                      "messages": [{"role": "user", "content": "Write a post"}],
                      "temperature": 0.8
                    }
                    """))
                .andRespond(withSuccess("""
                    {"choices":[{"message":{"role":"assistant","content":"A varied post"}}]}
                    """, MediaType.APPLICATION_JSON));

        assertEquals("A varied post", generator.generate("Write a post"));
        server.verify();
    }
}