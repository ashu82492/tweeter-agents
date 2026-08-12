package com.think9.app.metrics;

import com.think9.identity.domain.UserType;
import com.think9.identity.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.stereotype.Service;

@Service
public class MetricServiceImpl implements MetricService {
    private static final long ROLLING_WINDOW_MINUTES = 60;
    private final ConcurrentHashMap<Long, LongAdder> tweetBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, LongAdder> directMessageBuckets = new ConcurrentHashMap<>();
    private final LongAdder errors = new LongAdder();
    private final UserRepository userRepository;
    private final HealthEndpoint healthEndpoint;
    private final Clock clock;

    public MetricServiceImpl(UserRepository userRepository, HealthEndpoint healthEndpoint, Clock clock) {
        this.userRepository = userRepository;
        this.healthEndpoint = healthEndpoint;
        this.clock = clock;
    }

    @Override
    public void recordTweet(Instant occurredAt) {
        record(tweetBuckets, occurredAt);
    }

    @Override
    public void recordDirectMessage(Instant occurredAt) {
        record(directMessageBuckets, occurredAt);
    }

    @Override
    public void recordError() {
        errors.increment();
    }

    @Override
    public MetricsSnapshot snapshot() {
        Instant now = clock.instant();
        return new MetricsSnapshot(
                userRepository.countByType(UserType.SYSTEM_AGENT),
                userRepository.countByTypeAndLastActiveAtAfter(UserType.SYSTEM_AGENT, now.minus(Duration.ofMinutes(10))),
                rate(tweetBuckets, now),
                rate(directMessageBuckets, now),
                errors.sum(),
                healthEndpoint.health().getStatus().getCode());
    }

    private void record(ConcurrentHashMap<Long, LongAdder> buckets, Instant occurredAt) {
        buckets.computeIfAbsent(occurredAt.getEpochSecond() / 60, ignored -> new LongAdder()).increment();
    }

    private long rate(ConcurrentHashMap<Long, LongAdder> buckets, Instant now) {
        long currentMinute = now.getEpochSecond() / 60;
        long oldestMinute = currentMinute - ROLLING_WINDOW_MINUTES + 1;
        buckets.keySet().removeIf(minute -> minute < oldestMinute);
        long count = 0;
        for (long minute = oldestMinute; minute <= currentMinute; minute++) {
            LongAdder bucket = buckets.get(minute);
            if (bucket != null) {
                count += bucket.sum();
            }
        }
        return Math.round((double) count / ROLLING_WINDOW_MINUTES);
    }
}