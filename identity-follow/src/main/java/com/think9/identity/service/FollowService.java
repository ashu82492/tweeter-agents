package com.think9.identity.service;

import java.util.UUID;

public interface FollowService {
    FollowResult follow(UUID followerId, UUID followeeId, String correlationId);

    FollowResult unfollow(UUID followerId, UUID followeeId, String correlationId);

    record FollowResult(boolean following, boolean changed) {
    }
}