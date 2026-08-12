package com.think9.messaging.domain;

import java.time.Instant;
import java.util.UUID;

public record Message(UUID id, UUID chatId, UUID senderId, String content, String idempotencyKey, Instant createdAt) {
}