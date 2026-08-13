package com.nexus.messaging.domain;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record Chat(UUID id, String participantPairKey, Set<UUID> participantIds, Instant createdAt) {
    public Chat {
        participantIds = Set.copyOf(participantIds);
        if (participantIds.size() != 2) {
            throw new IllegalArgumentException("a chat must have exactly two participants");
        }
    }
}