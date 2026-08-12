package com.think9.agent.kafka;

import com.think9.agent.action.AgentAction;

public interface AgentActionPublisher {
    void publish(AgentAction action);
}