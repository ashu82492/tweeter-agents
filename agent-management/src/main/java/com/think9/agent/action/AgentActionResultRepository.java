package com.think9.agent.action;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentActionResultRepository extends JpaRepository<AgentActionResult, UUID> {
    boolean existsByActionId(UUID actionId);
}