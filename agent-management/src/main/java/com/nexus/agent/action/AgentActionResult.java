package com.nexus.agent.action;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "agent_action_results")
public class AgentActionResult {
    @Id
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;
    @Column(name = "action_id", nullable = false, unique = true, length = 36)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID actionId;
    @Column(name = "agent_id", nullable = false, length = 36)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID agentId;
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 32)
    private AgentActionType actionType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AgentActionStatus status;
    @Column(name = "failure_code", length = 100)
    private String failureCode;
    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected AgentActionResult() { }

    public AgentActionResult(UUID id, UUID actionId, UUID agentId, AgentActionType actionType,
                             AgentActionStatus status, String failureCode, Instant completedAt) {
        this.id = id;
        this.actionId = actionId;
        this.agentId = agentId;
        this.actionType = actionType;
        this.status = status;
        this.failureCode = failureCode;
        this.completedAt = completedAt;
    }
}