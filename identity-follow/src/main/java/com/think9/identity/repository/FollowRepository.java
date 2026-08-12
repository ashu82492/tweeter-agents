package com.think9.identity.repository;

import com.think9.identity.domain.Follow;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository {
    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    Follow save(Follow follow);

    void delete(Follow follow);

    List<UUID> findFollowerIdsByFolloweeId(UUID followeeId);
}