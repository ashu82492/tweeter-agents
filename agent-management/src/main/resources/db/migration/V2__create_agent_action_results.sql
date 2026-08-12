CREATE TABLE agent_action_results (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    action_id VARCHAR(36) NOT NULL UNIQUE,
    agent_id VARCHAR(36) NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    failure_code VARCHAR(100),
    completed_at TIMESTAMP(6) NOT NULL,
    INDEX idx_agent_action_results_agent_id (agent_id)
);