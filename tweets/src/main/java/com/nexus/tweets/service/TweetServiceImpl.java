package com.nexus.tweets.service;

import com.nexus.core.events.EventPublisher;
import com.nexus.core.events.TweetPublished;
import com.nexus.tweets.domain.Tweet;
import com.nexus.tweets.repository.TweetRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TweetServiceImpl implements TweetService {
    private static final int MAX_CONTENT_LENGTH = 280;
    private final TweetRepository tweetRepository;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    public TweetServiceImpl(TweetRepository tweetRepository, EventPublisher eventPublisher, Clock clock) {
        this.tweetRepository = tweetRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Tweet tweet(UUID authorId, String content, String idempotencyKey) {
        validateContent(content);
        return tweetRepository.findByAuthorIdAndIdempotencyKey(authorId, idempotencyKey)
                .orElseGet(() -> createTweet(authorId, content, idempotencyKey));
    }

    @Override
    @Transactional(readOnly = true)
    public Tweet getTweet(UUID tweetId) {
        return tweetRepository.findById(tweetId).orElseThrow(() -> new IllegalArgumentException("tweet not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tweet> getTweets(UUID authorId, int limit) {
        return tweetRepository.findRecentByAuthorId(authorId, Math.min(Math.max(limit, 1), 100));
    }

    private Tweet createTweet(UUID authorId, String content, String idempotencyKey) {
        Instant now = clock.instant();
        Tweet savedTweet = tweetRepository.save(new Tweet(UUID.randomUUID(), authorId, content.trim(), idempotencyKey, now, now));
        eventPublisher.publish(new TweetPublished(UUID.randomUUID(), savedTweet.id(), authorId, now, idempotencyKey));
        return savedTweet;
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank() || content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("tweet content must contain between 1 and 280 characters");
        }
    }
}