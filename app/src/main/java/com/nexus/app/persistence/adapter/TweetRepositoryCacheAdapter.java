package com.nexus.app.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.tweets.domain.Tweet;
import com.nexus.tweets.repository.TweetRepository;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class TweetRepositoryCacheAdapter implements TweetRepository {
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);
    private final TweetRepository database;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public TweetRepositoryCacheAdapter(TweetRepository database, StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.database = database;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public Tweet save(Tweet tweet) {
        Tweet saved = database.save(tweet);
        redis.opsForValue().set(key(tweet.id()), serialize(saved), CACHE_TTL);
        return saved;
    }

    @Override
    public Optional<Tweet> findById(UUID tweetId) {
        String cached = redis.opsForValue().get(key(tweetId));
        if (cached != null) return Optional.of(deserialize(cached));
        Optional<Tweet> tweet = database.findById(tweetId);
        tweet.ifPresent(value -> redis.opsForValue().set(key(value.id()), serialize(value), CACHE_TTL));
        return tweet;
    }

    @Override public Optional<Tweet> findByAuthorIdAndIdempotencyKey(UUID authorId, String key) { return database.findByAuthorIdAndIdempotencyKey(authorId, key); }
    @Override public List<Tweet> findRecentByAuthorId(UUID authorId, int limit) { return database.findRecentByAuthorId(authorId, limit); }

    private String key(UUID tweetId) { return "nexus:tweet:" + tweetId; }
    private String serialize(Tweet tweet) { try { return objectMapper.writeValueAsString(tweet); } catch (JsonProcessingException exception) { throw new IllegalStateException("could not cache tweet", exception); } }
    private Tweet deserialize(String value) { try { return objectMapper.readValue(value, Tweet.class); } catch (JsonProcessingException exception) { throw new IllegalStateException("could not read cached tweet", exception); } }
}