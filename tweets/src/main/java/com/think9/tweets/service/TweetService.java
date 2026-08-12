package com.think9.tweets.service;

import com.think9.tweets.domain.Tweet;
import java.util.List;
import java.util.UUID;

public interface TweetService {
    Tweet tweet(UUID authorId, String content, String idempotencyKey);
    Tweet getTweet(UUID tweetId);
    List<Tweet> getTweets(UUID authorId, int limit);
}