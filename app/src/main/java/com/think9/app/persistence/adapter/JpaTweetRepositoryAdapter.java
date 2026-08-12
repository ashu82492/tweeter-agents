package com.think9.app.persistence.adapter;

import com.think9.app.persistence.entity.TweetEntity;
import com.think9.app.persistence.jpa.TweetJpaRepository;
import com.think9.tweets.domain.Tweet;
import com.think9.tweets.repository.TweetRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository("jpaTweetRepositoryAdapter")
public class JpaTweetRepositoryAdapter implements TweetRepository {
    private final TweetJpaRepository repository;
    public JpaTweetRepositoryAdapter(TweetJpaRepository repository) { this.repository = repository; }
    @Override public Tweet save(Tweet tweet) { return repository.save(new TweetEntity(tweet)).toDomain(); }
    @Override public Optional<Tweet> findById(UUID tweetId) { return repository.findById(tweetId).map(TweetEntity::toDomain); }
    @Override public Optional<Tweet> findByAuthorIdAndIdempotencyKey(UUID authorId, String key) { return repository.findByAuthorIdAndIdempotencyKey(authorId, key).map(TweetEntity::toDomain); }
    @Override public List<Tweet> findRecentByAuthorId(UUID authorId, int limit) { return repository.findTop100ByAuthorIdOrderByCreatedAtDesc(authorId).stream().limit(limit).map(TweetEntity::toDomain).toList(); }
}