package com.nexus.app.persistence.adapter;

import com.nexus.app.persistence.entity.FollowEntity;
import com.nexus.app.persistence.jpa.FollowJpaRepository;
import com.nexus.identity.domain.Follow;
import com.nexus.identity.repository.FollowRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class FollowRepositoryAdapter implements FollowRepository {
    private final FollowJpaRepository repository;
    public FollowRepositoryAdapter(FollowJpaRepository repository) { this.repository = repository; }
    @Override public Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId) { return repository.findByFollowerIdAndFolloweeId(followerId, followeeId).map(FollowEntity::toDomain); }
    @Override public Follow save(Follow follow) { return repository.save(new FollowEntity(follow)).toDomain(); }
    @Override public void delete(Follow follow) { repository.deleteById(follow.id()); }
    @Override public List<UUID> findFollowerIdsByFolloweeId(UUID followeeId) { return repository.findByFolloweeId(followeeId).stream().map(entity -> entity.toDomain().followerId()).toList(); }
    @Override public List<UUID> findFolloweeIdsByFollowerId(UUID followerId) { return repository.findByFollowerId(followerId).stream().map(entity -> entity.toDomain().followeeId()).toList(); }
}