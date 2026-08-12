package com.think9.agent.scheduler;

import com.think9.agent.action.ActionSelector;
import com.think9.agent.action.AgentAction;
import com.think9.agent.action.AgentActionType;
import com.think9.agent.config.AgentManagementProperties;
import com.think9.agent.kafka.AgentActionPublisher;
import com.think9.agent.profile.AgentProfile;
import com.think9.agent.profile.AgentProfileRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AgentScheduler {
    private final AgentProfileRepository profileRepository;
    private final ActionSelector actionSelector;
    private final AgentActionPublisher actionPublisher;
    private final AgentManagementProperties.Scheduler scheduler;
    private final Clock clock;
    private final RandomGenerator randomGenerator;

    @Autowired
    public AgentScheduler(AgentProfileRepository profileRepository, ActionSelector actionSelector,
                          AgentActionPublisher actionPublisher, AgentManagementProperties properties) {
        this(profileRepository, actionSelector, actionPublisher, properties.getScheduler(), Clock.systemUTC(), new Random());
    }

    AgentScheduler(AgentProfileRepository profileRepository, ActionSelector actionSelector,
                   AgentActionPublisher actionPublisher, AgentManagementProperties.Scheduler scheduler,
                   Clock clock, RandomGenerator randomGenerator) {
        this.profileRepository = profileRepository;
        this.actionSelector = actionSelector;
        this.actionPublisher = actionPublisher;
        this.scheduler = scheduler;
        this.clock = clock;
        this.randomGenerator = randomGenerator;
    }

    @Scheduled(fixedDelayString = "${think9.agent.scheduler.poll-interval:30s}")
    @Transactional
    public void scheduleEligibleAgents() {
        Instant now = clock.instant();
        List<AgentProfile> profiles = profileRepository.findAll();
        for (AgentProfile profile : profileRepository.findByNextActionAtLessThanEqual(now)) {
            AgentActionType actionType = actionSelector.select();
            actionPublisher.publish(new AgentAction(UUID.randomUUID(), profile.getUserId(), actionType, now,
                    metadata(actionType, profile, profiles)));
            profile.scheduleNextActionAt(now.plus(nextDelay()));
        }
    }

    private Map<String, String> metadata(AgentActionType actionType, AgentProfile profile, List<AgentProfile> profiles) {
        if (actionType != AgentActionType.SEND_DM && actionType != AgentActionType.FOLLOW) {
            return Map.of();
        }
        List<AgentProfile> candidates = profiles.stream()
                .filter(candidate -> !candidate.getUserId().equals(profile.getUserId())).toList();
        if (candidates.isEmpty()) {
            return Map.of();
        }
        AgentProfile target = candidates.get(randomGenerator.nextInt(candidates.size()));
        return Map.of("targetUserId", target.getUserId().toString());
    }

    private Duration nextDelay() {
        scheduler.validateIntervals();
        long minimum = scheduler.getActionIntervalMin().toMillis();
        long maximum = scheduler.getActionIntervalMax().toMillis();
        return Duration.ofMillis(minimum == maximum ? minimum : randomGenerator.nextLong(minimum, maximum + 1));
    }
}