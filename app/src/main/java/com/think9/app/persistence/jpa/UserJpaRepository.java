package com.think9.app.persistence.jpa;

import com.think9.app.persistence.entity.UserEntity;
import com.think9.identity.domain.UserType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
    long countByType(UserType type);
    long countByTypeAndLastActiveAtAfter(UserType type, Instant cutoff);
}