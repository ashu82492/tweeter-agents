package com.think9.core.events;

import java.time.Instant;
import java.util.UUID;

public record FollowRemoved(
        UUID eventId,
        UUID aggregateId,
        UUID followerId,
        UUID followeeId,
        Instant occurredAt,
        String correlationId
) implements DomainEvent {
}