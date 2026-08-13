package com.think9.timeline.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.think9.core.events.TweetPublished;
import com.think9.identity.repository.FollowRepository;
import com.think9.timeline.repository.TimelineRepository;
import com.think9.tweets.repository.TweetRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimelineServiceImplTest {
    @Mock private TimelineRepository timelineRepository;
    @Mock private FollowRepository followRepository;
    @Mock private TweetRepository tweetRepository;

    @Test
    void onTweetPublished_addsTweetToEveryFollowerFeed() {
        UUID authorId = UUID.randomUUID();
        UUID tweetId = UUID.randomUUID();
        UUID firstFollower = UUID.randomUUID();
        UUID secondFollower = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-08-10T00:00:00Z");
        when(followRepository.findFollowerIdsByFolloweeId(authorId)).thenReturn(List.of(firstFollower, secondFollower));
        TimelineService service = new TimelineServiceImpl(timelineRepository, followRepository, tweetRepository, 20);

        service.onTweetPublished(new TweetPublished(UUID.randomUUID(), tweetId, authorId, occurredAt, "key"));

        verify(timelineRepository).add(firstFollower, tweetId, occurredAt.toEpochMilli());
        verify(timelineRepository).add(secondFollower, tweetId, occurredAt.toEpochMilli());
    }

    @Test
    void fetchFeed_forwardsOffsetAndBoundsLimit() {
        UUID userId = UUID.randomUUID();
        when(timelineRepository.fetch(userId, 4, 100)).thenReturn(List.of());
        TimelineService service = new TimelineServiceImpl(timelineRepository, followRepository, tweetRepository, 20);

        service.fetchFeed(userId, 4, 200);

        verify(timelineRepository).fetch(userId, 4, 100);
    }
}