package com.think9.agent.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("think9.agent")
public class AgentManagementProperties {
    @NotBlank
    private String apiBaseUrl = "http://localhost:8080/api/v1";
    @NotBlank
    private String credentialEncryptionKey;
    @NotBlank
    private String agentPassword;
    @Valid
    private final Kafka kafka = new Kafka();
    @Valid
    private final Scheduler scheduler = new Scheduler();
        @Valid
        private List<Personality> personalities = List.of(
            new Personality("Curious Builder", List.of("software", "science", "learning")),
            new Personality("Thoughtful Observer", List.of("books", "culture", "history")),
            new Personality("Playful Optimist", List.of("music", "games", "food")),
            new Personality("Practical Analyst", List.of("business", "design", "technology")));
    @Valid
    private final Llm llm = new Llm();
    @Valid
    private final Bootstrap bootstrap = new Bootstrap();
    @Valid
    private final Metrics metrics = new Metrics();

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getCredentialEncryptionKey() {
        return credentialEncryptionKey;
    }

    public void setCredentialEncryptionKey(String credentialEncryptionKey) {
        this.credentialEncryptionKey = credentialEncryptionKey;
    }

    public String getAgentPassword() {
        return agentPassword;
    }

    public void setAgentPassword(String agentPassword) {
        this.agentPassword = agentPassword;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public List<Personality> getPersonalities() {
        return personalities;
    }

    public void setPersonalities(List<Personality> personalities) {
        this.personalities = List.copyOf(personalities);
    }

    public Llm getLlm() {
        return llm;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public static class Kafka {
        @NotBlank
        private String bootstrapServers = "localhost:9092";
        @NotBlank
        private String topic = "agent-actions";
        @Min(1)
        private int workerCount = 10;

        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public int getWorkerCount() { return workerCount; }
        public void setWorkerCount(int workerCount) { this.workerCount = workerCount; }
    }

    public static class Scheduler {
        private Duration pollInterval = Duration.ofSeconds(30);
        private Duration actionIntervalMin = Duration.ofSeconds(8);
        private Duration actionIntervalMax = Duration.ofSeconds(31);
        @Min(0) @Max(100) private int tweetProbability = 50;
        @Min(0) @Max(100) private int directMessageProbability = 20;
        @Min(0) @Max(100) private int timelineProbability = 20;
        @Min(0) @Max(100) private int followProbability = 10;

        public Duration getPollInterval() { return pollInterval; }
        public void setPollInterval(Duration pollInterval) { this.pollInterval = pollInterval; }
        public Duration getActionIntervalMin() { return actionIntervalMin; }
        public void setActionIntervalMin(Duration actionIntervalMin) { this.actionIntervalMin = actionIntervalMin; }
        public Duration getActionIntervalMax() { return actionIntervalMax; }
        public void setActionIntervalMax(Duration actionIntervalMax) { this.actionIntervalMax = actionIntervalMax; }
        public int getTweetProbability() { return tweetProbability; }
        public void setTweetProbability(int tweetProbability) { this.tweetProbability = tweetProbability; }
        public int getDirectMessageProbability() { return directMessageProbability; }
        public void setDirectMessageProbability(int directMessageProbability) { this.directMessageProbability = directMessageProbability; }
        public int getTimelineProbability() { return timelineProbability; }
        public void setTimelineProbability(int timelineProbability) { this.timelineProbability = timelineProbability; }
        public int getFollowProbability() { return followProbability; }
        public void setFollowProbability(int followProbability) { this.followProbability = followProbability; }

        public void validateProbabilityTotal() {
            int total = tweetProbability + directMessageProbability + timelineProbability + followProbability;
            if (total != 100) {
                throw new IllegalStateException("agent action probabilities must total 100");
            }
        }

        public void validateIntervals() {
            if (actionIntervalMin.isNegative() || actionIntervalMax.isNegative()
                    || actionIntervalMin.compareTo(actionIntervalMax) > 0) {
                throw new IllegalStateException("agent action interval bounds are invalid");
            }
        }
    }

    public record Personality(@NotBlank String name, @NotEmpty List<@NotBlank String> interests) { }

    public static class Llm {
        @NotBlank private String provider = "lm-studio";
        @NotBlank private String baseUrl = "http://localhost:1234/v1";
        @NotBlank private String model = "local-model";
        private String apiKey;

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

    public static class Bootstrap {
        private boolean enabled;
        @Min(1) private int count = 100;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class Metrics {
        private String adminUsername;
        private String adminPassword;

        public String getAdminUsername() { return adminUsername; }
        public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
        public String getAdminPassword() { return adminPassword; }
        public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    }
}