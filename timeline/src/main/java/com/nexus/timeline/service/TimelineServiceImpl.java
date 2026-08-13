package com.nexus.timeline.service;

import com.nexus.core.events.FollowCreated;
import com.nexus.core.events.FollowRemoved;
import com.nexus.core.events.TweetPublished;
import com.nexus.identity.repository.FollowRepository;
import com.nexus.timeline.repository.TimelineRepository;
import com.nexus.tweets.domain.Tweet;
import com.nexus.tweets.repository.TweetRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TimelineServiceImpl implements TimelineService {
    private final TimelineRepository timelineRepository;
    private final FollowRepository followRepository;
    private final TweetRepository tweetRepository;
    private final int recentTweetCount;

    public TimelineServiceImpl(TimelineRepository timelineRepository, FollowRepository followRepository,
                               TweetRepository tweetRepository, int recentTweetCount) {
        this.timelineRepository = timelineRepository;
        this.followRepository = followRepository;
        this.tweetRepository = tweetRepository;
        this.recentTweetCount = recentTweetCount;
    }

    @Override
    public List<UUID> fetchFeed(UUID userId, int offset, int limit) {
        int boundedOffset = Math.max(offset, 0);
        int boundedLimit = Math.min(Math.max(limit, 1), 100);
        return timelineRepository.fetch(userId, boundedOffset, boundedLimit);
    }

    @Override
    public void onTweetPublished(TweetPublished event) {
        long score = event.occurredAt().toEpochMilli();
        for (UUID followerId : followRepository.findFollowerIdsByFolloweeId(event.authorId())) {
            timelineRepository.add(followerId, event.aggregateId(), score);
        }
    }

    @Override
    public void onFollowCreated(FollowCreated event) {
        for (Tweet tweet : tweetRepository.findRecentByAuthorId(event.followeeId(), recentTweetCount)) {
            timelineRepository.add(event.followerId(), tweet.id(), tweet.createdAt().toEpochMilli());
        }
    }

    @Override
    public void onFollowRemoved(FollowRemoved event) {
        for (Tweet tweet : tweetRepository.findRecentByAuthorId(event.followeeId(), recentTweetCount)) {
            timelineRepository.remove(event.followerId(), tweet.id());
        }
    }
}