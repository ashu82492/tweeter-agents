package com.nexus.tweets.repository;

import com.nexus.tweets.domain.Tweet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TweetRepository {
    Tweet save(Tweet tweet);
    Optional<Tweet> findById(UUID tweetId);
    Optional<Tweet> findByAuthorIdAndIdempotencyKey(UUID authorId, String idempotencyKey);
    List<Tweet> findRecentByAuthorId(UUID authorId, int limit);
}