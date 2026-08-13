package com.nexus.agent.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexus.agent.config.AgentManagementProperties;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class WeightedActionSelectorTest {
    @Test
    void select_usesConfiguredActionBoundaries() {
        AgentManagementProperties.Scheduler scheduler = new AgentManagementProperties.Scheduler();

        assertEquals(AgentActionType.TWEET, new WeightedActionSelector(scheduler, random(49)).select());
        assertEquals(AgentActionType.SEND_DM, new WeightedActionSelector(scheduler, random(50)).select());
        assertEquals(AgentActionType.READ_TIMELINE, new WeightedActionSelector(scheduler, random(70)).select());
        assertEquals(AgentActionType.FOLLOW, new WeightedActionSelector(scheduler, random(90)).select());
    }

    @Test
    void select_rejectsInvalidProbabilityTotal() {
        AgentManagementProperties.Scheduler scheduler = new AgentManagementProperties.Scheduler();
        scheduler.setFollowProbability(9);

        assertThrows(IllegalStateException.class, () -> new WeightedActionSelector(scheduler, random(0)).select());
    }

    private RandomGenerator random(int selection) {
        return new RandomGenerator() {
            @Override public long nextLong() { return selection; }
            @Override public int nextInt(int bound) { return selection; }
        };
    }
}