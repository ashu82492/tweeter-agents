package com.nexus.tweets.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexus.core.events.EventPublisher;
import com.nexus.tweets.domain.Tweet;
import com.nexus.tweets.repository.TweetRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TweetServiceImplTest {
    @Mock private TweetRepository tweetRepository;
    @Mock private EventPublisher eventPublisher;

    @Test
    void tweet_returnsOriginalTweetWithoutPublishingAgain_whenIdempotencyKeyAlreadyExists() {
        UUID authorId = UUID.randomUUID();
        Tweet existing = new Tweet(UUID.randomUUID(), authorId, "Existing", "key-1", Instant.EPOCH, Instant.EPOCH);
        when(tweetRepository.findByAuthorIdAndIdempotencyKey(authorId, "key-1")).thenReturn(Optional.of(existing));
        TweetService service = new TweetServiceImpl(tweetRepository, eventPublisher, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        Tweet result = service.tweet(authorId, "Existing", "key-1");

        assertEquals(existing, result);
        verify(tweetRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(eventPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }
}