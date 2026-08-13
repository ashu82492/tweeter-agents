package com.nexus.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String username,
        String passwordHash,
        String displayName,
        UserType type,
        boolean enabled,
        Instant lastActiveAt,
        Instant createdAt,
        Instant updatedAt
) {
}