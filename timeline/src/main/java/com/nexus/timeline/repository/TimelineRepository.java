package com.nexus.timeline.repository;

import java.util.List;
import java.util.UUID;

public interface TimelineRepository {
    void add(UUID recipientId, UUID tweetId, long score);
    void remove(UUID recipientId, UUID tweetId);
    List<UUID> fetch(UUID recipientId, int offset, int limit);
}