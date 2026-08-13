package com.nexus.tweets.domain;

import java.time.Instant;
import java.util.UUID;

public record Tweet(UUID id, UUID authorId, String content, String idempotencyKey, Instant createdAt, Instant updatedAt) {
}