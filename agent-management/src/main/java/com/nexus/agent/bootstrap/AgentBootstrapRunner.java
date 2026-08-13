package com.nexus.agent.bootstrap;

import com.nexus.agent.config.AgentManagementProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AgentBootstrapRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentBootstrapRunner.class);
    private final AgentBootstrapService bootstrapService;
    private final AgentManagementProperties properties;

    public AgentBootstrapRunner(AgentBootstrapService bootstrapService, AgentManagementProperties properties) {
        this.bootstrapService = bootstrapService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (!properties.getBootstrap().isEnabled()) {
            return;
        }
        int createdOrLoaded = bootstrapService.bootstrap(properties.getBootstrap().getCount()).size();
        LOGGER.info("Agent bootstrap completed count={}", createdOrLoaded);
    }
}