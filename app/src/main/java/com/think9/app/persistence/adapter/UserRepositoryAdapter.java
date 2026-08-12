package com.think9.app.persistence.adapter;

import com.think9.app.persistence.entity.UserEntity;
import com.think9.app.persistence.jpa.UserJpaRepository;
import com.think9.identity.domain.User;
import com.think9.identity.domain.UserType;
import com.think9.identity.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository repository;
    public UserRepositoryAdapter(UserJpaRepository repository) { this.repository = repository; }
    @Override public User save(User user) { return repository.save(new UserEntity(user)).toDomain(); }
    @Override public Optional<User> findById(UUID userId) { return repository.findById(userId).map(UserEntity::toDomain); }
    @Override public Optional<User> findByUsername(String username) { return repository.findByUsername(username).map(UserEntity::toDomain); }
    @Override public long countByType(UserType type) { return repository.countByType(type); }
    @Override public long countByTypeAndLastActiveAtAfter(UserType type, Instant cutoff) { return repository.countByTypeAndLastActiveAtAfter(type, cutoff); }
}