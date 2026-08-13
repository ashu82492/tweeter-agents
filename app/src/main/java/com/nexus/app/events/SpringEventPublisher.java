package com.nexus.app.events;

import com.nexus.core.events.DomainEvent;
import com.nexus.core.events.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisher implements EventPublisher {
    private final ApplicationEventPublisher publisher;
    public SpringEventPublisher(ApplicationEventPublisher publisher) { this.publisher = publisher; }
    @Override public void publish(DomainEvent event) { publisher.publishEvent(event); }
}