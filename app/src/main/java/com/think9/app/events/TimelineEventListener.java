package com.think9.app.events;

import com.think9.core.events.FollowCreated;
import com.think9.core.events.FollowRemoved;
import com.think9.core.events.TweetPublished;
import com.think9.timeline.service.TimelineService;
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