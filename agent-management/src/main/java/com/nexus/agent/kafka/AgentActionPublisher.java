package com.nexus.agent.kafka;

import com.nexus.agent.action.AgentAction;

public interface AgentActionPublisher {
    void publish(AgentAction action);
}