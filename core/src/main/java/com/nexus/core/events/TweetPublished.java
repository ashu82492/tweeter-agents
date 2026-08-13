package com.nexus.core.events;

import java.time.Instant;
import java.util.UUID;

public record TweetPublished(
        UUID eventId,
        UUID aggregateId,
        UUID authorId,
        Instant occurredAt,
        String correlationId
) implements DomainEvent {
}