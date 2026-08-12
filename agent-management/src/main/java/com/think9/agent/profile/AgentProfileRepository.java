package com.think9.agent.profile;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentProfileRepository extends JpaRepository<AgentProfile, UUID> {
    Optional<AgentProfile> findByUsername(String username);

    Optional<AgentProfile> findByUserId(UUID userId);

    java.util.List<AgentProfile> findByNextActionAtLessThanEqual(java.time.Instant nextActionAt);
}