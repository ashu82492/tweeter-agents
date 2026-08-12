package com.think9.agent.action;

import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentActionResultService {
    private final AgentActionResultRepository repository;
    private final Clock clock;

    @Autowired
    public AgentActionResultService(AgentActionResultRepository repository) {
        this(repository, Clock.systemUTC());
    }

    AgentActionResultService(AgentActionResultRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public boolean isCompleted(UUID actionId) {
        return repository.existsByActionId(actionId);
    }

    public void recordSuccess(AgentAction action) {
        save(action, AgentActionStatus.SUCCEEDED, null);
    }

    public void recordFailure(AgentAction action, RuntimeException exception) {
        save(action, AgentActionStatus.FAILED, exception.getClass().getSimpleName());
    }

    private void save(AgentAction action, AgentActionStatus status, String failureCode) {
        repository.save(new AgentActionResult(UUID.randomUUID(), action.actionId(), action.agentId(),
                action.actionType(), status, failureCode, clock.instant()));
    }
}