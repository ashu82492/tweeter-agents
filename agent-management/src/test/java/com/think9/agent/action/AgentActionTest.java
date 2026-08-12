package com.think9.agent.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AgentActionTest {
    @Test
    void createsImmutableActionWithRequiredFields() {
        UUID actionId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        AgentAction action = new AgentAction(actionId, agentId, AgentActionType.TWEET,
                Instant.parse("2026-08-11T10:00:00Z"), Map.of("topic", "java"));

        assertEquals(actionId, action.actionId());
        assertEquals(agentId, action.agentId());
        assertEquals("java", action.metadata().get("topic"));
        assertThrows(UnsupportedOperationException.class, () -> action.metadata().put("topic", "spring"));
    }
}