package com.think9.app.persistence.jpa;

import com.think9.app.persistence.entity.TweetEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TweetJpaRepository extends JpaRepository<TweetEntity, UUID> {
    Optional<TweetEntity> findByAuthorIdAndIdempotencyKey(UUID authorId, String idempotencyKey);
    List<TweetEntity> findTop100ByAuthorIdOrderByCreatedAtDesc(UUID authorId);
}