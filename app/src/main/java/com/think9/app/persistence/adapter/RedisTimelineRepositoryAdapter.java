package com.think9.app.persistence.adapter;

import com.think9.timeline.repository.TimelineRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.data.redis.core.StringRedisTemplate;

@Repository
public class RedisTimelineRepositoryAdapter implements TimelineRepository {
    private final StringRedisTemplate redis;
    public RedisTimelineRepositoryAdapter(StringRedisTemplate redis) { this.redis = redis; }
    @Override public void add(UUID recipientId, UUID tweetId, long score) { redis.opsForZSet().add(key(recipientId), tweetId.toString(), score); }
    @Override public void remove(UUID recipientId, UUID tweetId) { redis.opsForZSet().remove(key(recipientId), tweetId.toString()); }
    @Override public List<UUID> fetch(UUID recipientId, int offset, int limit) {
        long start = Math.max(0, offset);
        long end = start + Math.max(0, limit - 1);
        return redis.opsForZSet().reverseRange(key(recipientId), start, end).stream().map(UUID::fromString).toList();
    }
    private String key(UUID recipientId) { return "think9:timeline:" + recipientId; }
}