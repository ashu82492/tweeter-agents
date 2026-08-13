package com.nexus.app.events;

import com.nexus.core.events.FollowCreated;
import com.nexus.core.events.FollowRemoved;
import com.nexus.core.events.TweetPublished;
import com.nexus.timeline.service.TimelineService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TimelineEventListener {
    private final TimelineService timelineService;
    public TimelineEventListener(TimelineService timelineService) { this.timelineService = timelineService; }
    @EventListener void on(TweetPublished event) { timelineService.onTweetPublished(event); }
    @EventListener void on(FollowCreated event) { timelineService.onFollowCreated(event); }
    @EventListener void on(FollowRemoved event) { timelineService.onFollowRemoved(event); }
}