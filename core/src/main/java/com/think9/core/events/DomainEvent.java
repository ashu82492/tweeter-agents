package com.think9.core.events;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();

    UUID aggregateId();

    Instant occurredAt();

    String correlationId();
}