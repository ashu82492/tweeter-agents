package com.think9.agent.kafka;

import com.think9.agent.config.AgentManagementProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {
    @Bean
    NewTopic agentActionsTopic(AgentManagementProperties properties) {
        return TopicBuilder.name(properties.getKafka().getTopic())
                .partitions(50)
                .replicas(1)
                .build();
    }
}