package com.nexus.app.metrics;

import com.nexus.identity.domain.User;
import com.nexus.identity.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class UserActivityService {
    private static final Duration WRITE_INTERVAL = Duration.ofMinutes(5);
    private final ConcurrentHashMap<UUID, Instant> lastWriteAt = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final Clock clock;

    public UserActivityService(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public void markActive(UUID userId) {
        Instant now = clock.instant();
        lastWriteAt.compute(userId, (ignored, lastWrite) -> {
            if (lastWrite != null && lastWrite.plus(WRITE_INTERVAL).isAfter(now)) {
                return lastWrite;
            }
            userRepository.findById(userId).ifPresent(user -> userRepository.save(withActivity(user, now)));
            return now;
        });
    }

    private User withActivity(User user, Instant now) {
        return new User(user.id(), user.username(), user.passwordHash(), user.displayName(), user.type(),
                user.enabled(), now, user.createdAt(), now);
    }
}