package com.nexus.identity.repository;

import com.nexus.identity.domain.Follow;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository {
    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    Follow save(Follow follow);

    void delete(Follow follow);

    List<UUID> findFollowerIdsByFolloweeId(UUID followeeId);
}