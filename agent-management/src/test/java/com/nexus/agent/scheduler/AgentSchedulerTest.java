package com.nexus.agent.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.agent.action.ActionSelector;
import com.nexus.agent.action.AgentActionType;
import com.nexus.agent.config.AgentManagementProperties;
import com.nexus.agent.kafka.AgentActionPublisher;
import com.nexus.agent.profile.AgentProfile;
import com.nexus.agent.profile.AgentProfileRepository;
import com.nexus.agent.profile.EncryptedCredential;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentSchedulerTest {
    @Mock private AgentProfileRepository profileRepository;
    @Mock private ActionSelector actionSelector;
    @Mock private AgentActionPublisher actionPublisher;

    @Test
    void scheduleEligibleAgents_publishesAgentKeyedActionAndStaggersNextRun() {
        Instant now = Instant.parse("2026-08-11T10:00:00Z");
        AgentProfile due = profile("agent-0001", now.minusSeconds(1));
        AgentProfile target = profile("agent-0002", now.plusSeconds(1));
        when(profileRepository.findAll()).thenReturn(List.of(due, target));
        when(profileRepository.findByNextActionAtLessThanEqual(now)).thenReturn(List.of(due));
        when(actionSelector.select()).thenReturn(AgentActionType.SEND_DM);
        AgentManagementProperties.Scheduler schedulerProperties = new AgentManagementProperties().getScheduler();

        new AgentScheduler(profileRepository, actionSelector, actionPublisher, schedulerProperties,
                Clock.fixed(now, ZoneOffset.UTC), random(0)).scheduleEligibleAgents();

        ArgumentCaptor<com.nexus.agent.action.AgentAction> action = ArgumentCaptor.forClass(com.nexus.agent.action.AgentAction.class);
        verify(actionPublisher).publish(action.capture());
        org.junit.jupiter.api.Assertions.assertEquals(due.getUserId(), action.getValue().agentId());
        org.junit.jupiter.api.Assertions.assertEquals(target.getUserId().toString(), action.getValue().metadata().get("targetUserId"));
        org.junit.jupiter.api.Assertions.assertTrue(due.getNextActionAt().isAfter(now));
    }

    private AgentProfile profile(String username, Instant nextActionAt) {
        return new AgentProfile(UUID.randomUUID(), UUID.randomUUID(), username, username, "Curious", List.of("software"),
                new EncryptedCredential("cipher", "iv"), Instant.EPOCH, nextActionAt);
    }

    private RandomGenerator random(int value) {
        return new RandomGenerator() {
            @Override public long nextLong() { return value; }
            @Override public int nextInt(int bound) { return value; }
            @Override public long nextLong(long origin, long bound) { return origin; }
        };
    }
}