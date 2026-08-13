package com.think9.timeline.service;

import com.think9.core.events.FollowCreated;
import com.think9.core.events.FollowRemoved;
import com.think9.core.events.TweetPublished;
import java.util.List;
import java.util.UUID;

public interface TimelineService {
    List<UUID> fetchFeed(UUID userId, int offset, int limit);
    void onTweetPublished(TweetPublished event);
    void onFollowCreated(FollowCreated event);
    void onFollowRemoved(FollowRemoved event);
}