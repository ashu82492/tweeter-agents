package com.think9.core.events;

import java.time.Instant;
import java.util.UUID;

public record MessageCreated(
        UUID eventId,
        UUID aggregateId,
        UUID senderId,
        Instant occurredAt,
        String correlationId
) implements DomainEvent {
}