package com.think9.agent.kafka;

import com.think9.agent.action.AgentAction;
import com.think9.agent.config.AgentManagementProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaAgentActionPublisher implements AgentActionPublisher {
    private final KafkaTemplate<String, AgentAction> kafkaTemplate;
    private final String topic;

    public KafkaAgentActionPublisher(KafkaTemplate<String, AgentAction> kafkaTemplate, AgentManagementProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = properties.getKafka().getTopic();
    }

    @Override
    public void publish(AgentAction action) {
        kafkaTemplate.send(topic, action.agentId().toString(), action);
    }
}