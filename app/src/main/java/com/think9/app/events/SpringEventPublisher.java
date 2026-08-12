package com.think9.app.events;

import com.think9.core.events.DomainEvent;
import com.think9.core.events.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisher implements EventPublisher {
    private final ApplicationEventPublisher publisher;
    public SpringEventPublisher(ApplicationEventPublisher publisher) { this.publisher = publisher; }
    @Override public void publish(DomainEvent event) { publisher.publishEvent(event); }
}