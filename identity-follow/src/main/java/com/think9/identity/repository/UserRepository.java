package com.think9.identity.repository;

import com.think9.identity.domain.User;
import com.think9.identity.domain.UserType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(UUID userId);

    Optional<User> findByUsername(String username);

    Optional<User> findFirstByType(UserType type);

    List<User> findAll(int limit);

    long countByType(UserType type);

    long countByTypeAndLastActiveAtAfter(UserType type, Instant cutoff);
}