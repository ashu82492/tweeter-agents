package com.think9.core.events;

public interface EventPublisher {
    void publish(DomainEvent event);
}