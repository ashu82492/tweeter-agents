package com.think9.identity.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.think9.core.events.EventPublisher;
import com.think9.identity.domain.Follow;
import com.think9.identity.repository.FollowRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {
    @Mock
    private FollowRepository followRepository;
    @Mock
    private EventPublisher eventPublisher;

    private final Clock clock = Clock.fixed(Instant.parse("2026-08-10T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void follow_returnsExistingStateWithoutPublishing_whenRelationshipAlreadyExists() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        when(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                .thenReturn(Optional.of(new Follow(UUID.randomUUID(), followerId, followeeId, clock.instant())));
        FollowService service = new FollowServiceImpl(followRepository, eventPublisher, clock);

        FollowService.FollowResult result = service.follow(followerId, followeeId, "request-1");

        assertAll(() -> assertTrue(result.following()), () -> assertFalse(result.changed()));
        verify(followRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unfollow_publishesRemoval_whenRelationshipExists() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Follow follow = new Follow(UUID.randomUUID(), followerId, followeeId, clock.instant());
        when(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)).thenReturn(Optional.of(follow));
        FollowService service = new FollowServiceImpl(followRepository, eventPublisher, clock);

        FollowService.FollowResult result = service.unfollow(followerId, followeeId, "request-2");

        assertAll(() -> assertFalse(result.following()), () -> assertTrue(result.changed()));
        verify(followRepository).delete(follow);
        verify(eventPublisher).publish(org.mockito.ArgumentMatchers.any());
    }
}