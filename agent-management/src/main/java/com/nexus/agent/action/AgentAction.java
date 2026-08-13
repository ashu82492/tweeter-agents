package com.nexus.agent.action;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AgentAction(
        UUID actionId,
        UUID agentId,
        AgentActionType actionType,
        Instant createdAt,
        Map<String, String> metadata
) {
    public AgentAction {
        if (actionId == null || agentId == null || actionType == null || createdAt == null) {
            throw new IllegalArgumentException("agent action fields must be present");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}