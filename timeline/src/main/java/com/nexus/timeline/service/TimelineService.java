package com.nexus.timeline.service;

import com.nexus.core.events.FollowCreated;
import com.nexus.core.events.FollowRemoved;
import com.nexus.core.events.TweetPublished;
import java.util.List;
import java.util.UUID;

public interface TimelineService {
    List<UUID> fetchFeed(UUID userId, int offset, int limit);
    void onTweetPublished(TweetPublished event);
    void onFollowCreated(FollowCreated event);
    void onFollowRemoved(FollowRemoved event);
}