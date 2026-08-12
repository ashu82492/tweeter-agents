package com.think9.identity.service;

import com.think9.core.events.EventPublisher;
import com.think9.core.events.FollowCreated;
import com.think9.core.events.FollowRemoved;
import com.think9.identity.domain.Follow;
import com.think9.identity.repository.FollowRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    public FollowServiceImpl(FollowRepository followRepository, EventPublisher eventPublisher, Clock clock) {
        this.followRepository = followRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public FollowResult follow(UUID followerId, UUID followeeId, String correlationId) {
        rejectSelfFollow(followerId, followeeId);
        if (followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId).isPresent()) {
            return new FollowResult(true, false);
        }
        Follow follow = followRepository.save(new Follow(UUID.randomUUID(), followerId, followeeId, clock.instant()));
        eventPublisher.publish(new FollowCreated(UUID.randomUUID(), follow.id(), followerId, followeeId,
                clock.instant(), correlationId));
        return new FollowResult(true, true);
    }

    @Override
    @Transactional
    public FollowResult unfollow(UUID followerId, UUID followeeId, String correlationId) {
        return followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .map(follow -> remove(follow, correlationId))
                .orElseGet(() -> new FollowResult(false, false));
    }

    private FollowResult remove(Follow follow, String correlationId) {
        followRepository.delete(follow);
        eventPublisher.publish(new FollowRemoved(UUID.randomUUID(), follow.id(), follow.followerId(), follow.followeeId(),
                clock.instant(), correlationId));
        return new FollowResult(false, true);
    }

    private void rejectSelfFollow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("a user cannot follow itself");
        }
    }
}