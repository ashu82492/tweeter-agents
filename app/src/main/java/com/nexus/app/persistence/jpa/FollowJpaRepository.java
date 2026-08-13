package com.nexus.app.persistence.jpa;

import com.nexus.app.persistence.entity.FollowEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowJpaRepository extends JpaRepository<FollowEntity, UUID> {
    Optional<FollowEntity> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    List<FollowEntity> findByFolloweeId(UUID followeeId);
}