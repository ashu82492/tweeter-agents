package com.think9.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record Follow(UUID id, UUID followerId, UUID followeeId, Instant createdAt) {
}