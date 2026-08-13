package com.nexus.agent;

import com.nexus.agent.config.AgentManagementProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AgentManagementProperties.class)
public class AgentManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentManagementApplication.class, args);
    }
}