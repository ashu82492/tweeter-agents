package com.nexus.core.events;

public interface EventPublisher {
    void publish(DomainEvent event);
}