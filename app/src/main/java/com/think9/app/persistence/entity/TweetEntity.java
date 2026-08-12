package com.think9.app.persistence.entity;

import com.think9.tweets.domain.Tweet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "tweets")
public class TweetEntity {
    @Id @JdbcTypeCode(Types.CHAR) private UUID id;
    @Column(name = "author_id", nullable = false) @JdbcTypeCode(Types.CHAR) private UUID authorId;
    @Column(nullable = false, length = 4000) private String content;
    @Column(name = "idempotency_key", nullable = false, length = 128) private String idempotencyKey;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected TweetEntity() { }
    public TweetEntity(Tweet tweet) { id = tweet.id(); authorId = tweet.authorId(); content = tweet.content(); idempotencyKey = tweet.idempotencyKey(); createdAt = tweet.createdAt(); updatedAt = tweet.updatedAt(); }
    public Tweet toDomain() { return new Tweet(id, authorId, content, idempotencyKey, createdAt, updatedAt); }
}