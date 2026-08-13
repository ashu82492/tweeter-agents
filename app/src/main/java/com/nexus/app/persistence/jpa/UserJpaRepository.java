package com.nexus.app.persistence.jpa;

import com.nexus.app.persistence.entity.UserEntity;
import com.nexus.identity.domain.UserType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findFirstByTypeOrderByCreatedAtAsc(UserType type);
    List<UserEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByType(UserType type);
    long countByTypeAndLastActiveAtAfter(UserType type, Instant cutoff);
}