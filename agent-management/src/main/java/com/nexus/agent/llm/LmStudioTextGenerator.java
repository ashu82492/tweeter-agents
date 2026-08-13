package com.nexus.agent.llm;

import com.nexus.agent.config.AgentManagementProperties;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LmStudioTextGenerator implements TextGenerator {
    private final RestClient restClient;
    private final AgentManagementProperties.Llm properties;

    public LmStudioTextGenerator(RestClient.Builder builder, AgentManagementProperties properties) {
        this.restClient = builder.baseUrl(properties.getLlm().getBaseUrl()).build();
        this.properties = properties.getLlm();
    }

    @Override
    public String generate(String prompt) {
        CompletionResponse response = restClient.post().uri("/chat/completions")
                .headers(this::authorizeWhenConfigured).contentType(MediaType.APPLICATION_JSON)
                .body(new CompletionRequest(properties.getModel(), List.of(new ChatMessage("user", prompt)),
                        properties.getTemperature()))
                .retrieve().body(CompletionResponse.class);
        if (response == null || response.choices() == null || response.choices().isEmpty()
                || response.choices().getFirst().message() == null || response.choices().getFirst().message().content() == null) {
            throw new IllegalStateException("LM Studio returned no generated content");
        }
        return response.choices().getFirst().message().content().trim();
    }

    private void authorizeWhenConfigured(HttpHeaders headers) {
        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            headers.setBearerAuth(properties.getApiKey());
        }
    }

    private record CompletionRequest(String model, List<ChatMessage> messages, double temperature) { }
    private record ChatMessage(String role, String content) { }
    private record CompletionResponse(List<Choice> choices) { }
    private record Choice(ChatMessage message) { }
}