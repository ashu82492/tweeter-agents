package com.think9.timeline.service;

import com.think9.core.events.FollowCreated;
import com.think9.core.events.FollowRemoved;
import com.think9.core.events.TweetPublished;
import com.think9.identity.repository.FollowRepository;
import com.think9.timeline.repository.TimelineRepository;
import com.think9.tweets.domain.Tweet;
import com.think9.tweets.repository.TweetRepository;
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