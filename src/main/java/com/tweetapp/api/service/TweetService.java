package com.tweetapp.api.service;

import java.util.List;

import com.tweetapp.api.exception.IncorrectOrDeletedTweet;
import com.tweetapp.api.model.Tweet;

public interface TweetService {

	Tweet postTweet(Tweet tweet);

	Tweet postTweetByUsername(Tweet tweet, String username);

	Tweet editTweet(Tweet tweet, String id);

	Tweet likeTweet(Tweet tweet);

	Tweet replyTweet(Tweet parentTweet, Tweet replyTweet);

	void deleteTweet(Tweet tweet);

	List<Tweet> getAllTweets();

	List<Tweet> getAllTweetsByUsername(String username);

	Tweet replyTweetById(Tweet replyTweet, String parentTweetId, String username) throws IncorrectOrDeletedTweet;

	void deleteTweetById(String username, String tweetId);

	String likeTweetById(String username, String tweetId);

	Tweet getTweetById(String id);

}
