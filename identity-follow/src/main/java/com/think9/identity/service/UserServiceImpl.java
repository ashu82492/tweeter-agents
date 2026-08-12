package com.think9.identity.service;

import com.think9.identity.domain.User;
import com.think9.identity.domain.UserType;
import com.think9.identity.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public User create(String username, String password, String displayName) {
        User existing = userRepository.findByUsername(username).orElse(null);
        if (existing != null) {
            if (passwordEncoder.matches(password, existing.passwordHash())) {
                return existing;
            }
            throw new IllegalArgumentException("username is already in use");
        }
        Instant now = clock.instant();
        User user = new User(UUID.randomUUID(), username, passwordEncoder.encode(password), displayName,
            UserType.SYSTEM_AGENT, true, null, now, now);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User get(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> list(int limit, UUID excludedUserId) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }
        return userRepository.findAll(limit).stream()
                .filter(user -> !user.id().equals(excludedUserId))
                .toList();
    }
}