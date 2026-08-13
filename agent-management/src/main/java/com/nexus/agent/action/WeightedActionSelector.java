package com.nexus.agent.action;

import com.nexus.agent.config.AgentManagementProperties;
import java.util.Random;
import java.util.random.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WeightedActionSelector implements ActionSelector {
    private final AgentManagementProperties.Scheduler scheduler;
    private final RandomGenerator randomGenerator;

    @Autowired
    public WeightedActionSelector(AgentManagementProperties properties) {
        this(properties.getScheduler(), new Random());
    }

    WeightedActionSelector(AgentManagementProperties.Scheduler scheduler, RandomGenerator randomGenerator) {
        this.scheduler = scheduler;
        this.randomGenerator = randomGenerator;
    }

    @Override
    public AgentActionType select() {
        scheduler.validateProbabilityTotal();
        int selection = randomGenerator.nextInt(100);
        int boundary = scheduler.getTweetProbability();
        if (selection < boundary) {
            return AgentActionType.TWEET;
        }
        boundary += scheduler.getDirectMessageProbability();
        if (selection < boundary) {
            return AgentActionType.SEND_DM;
        }
        boundary += scheduler.getTimelineProbability();
        return selection < boundary ? AgentActionType.READ_TIMELINE : AgentActionType.FOLLOW;
    }
}