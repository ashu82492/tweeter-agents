package com.think9.agent.kafka;

import static org.mockito.Mockito.verify;

import com.think9.agent.action.AgentAction;
import com.think9.agent.action.AgentActionType;
import com.think9.agent.config.AgentManagementProperties;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class KafkaAgentActionPublisherTest {
    @Mock
    private KafkaTemplate<String, AgentAction> kafkaTemplate;

    @Test
    void publish_usesAgentIdAsKafkaRecordKey() {
        AgentManagementProperties properties = new AgentManagementProperties();
        properties.getKafka().setTopic("agent-actions-test");
        UUID agentId = UUID.randomUUID();
        AgentAction action = new AgentAction(UUID.randomUUID(), agentId, AgentActionType.TWEET, Instant.now(), Map.of());

        new KafkaAgentActionPublisher(kafkaTemplate, properties).publish(action);

        verify(kafkaTemplate).send("agent-actions-test", agentId.toString(), action);
    }
}