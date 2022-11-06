package com.tweetapp.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tweetapp.api.exception.IncorrectOrDeletedTweet;
import com.tweetapp.api.kafka.ProducerService;
import com.tweetapp.api.model.Likes;
import com.tweetapp.api.model.Tweet;
import com.tweetapp.api.model.User;
import com.tweetapp.api.repository.LikeRepository;
import com.tweetapp.api.repository.TweetRepository;
import com.tweetapp.api.repository.UserRepository;

@Service
public class TweetServiceImpl implements TweetService {

	@Autowired
	TweetRepository tweetRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ProducerService producerService;

	@Autowired
	LikeRepository likeRepository;

	static int countForTweetCreation = 0;

	Logger logger = LoggerFactory.getLogger(TweetServiceImpl.class);

	@Override
	public Tweet postTweet(Tweet tweet) {

		producerService.sendMessage("Tweet posted by user");
		logger.info("Tweet posted successfully....");
		return tweetRepository.save(tweet);
	}

	@Override
	public Tweet editTweet(Tweet tweet, String id) {

		// producerService.sendMessage("Tweet is updated..");
		Tweet tweetold = tweetRepository.findById(id).get();
		tweetold.setTweetName(tweet.getTweetName());
		tweetold.setTweetTag(tweet.getTweetTag());
		logger.info("Tweet is updated successfully...");

		return tweetRepository.save(tweetold);
	}

	@Override
	public Tweet likeTweet(Tweet tweet) {

		tweet.setLikes(tweet.getLikes() + 1);
		return tweetRepository.save(tweet);
	}

	@Override
	public Tweet replyTweet(Tweet parentTweet, Tweet replyTweet) {
		tweetRepository.save(replyTweet);
		List<Tweet> parentTweetReplies = parentTweet.getReplies();
		parentTweetReplies.add(replyTweet);
		parentTweet.setReplies(parentTweetReplies);
		tweetRepository.save(parentTweet);
		return replyTweet;
	}

	@Override
	public void deleteTweet(Tweet tweet) {
		tweetRepository.delete(tweet);
		logger.info("Tweet deleted successfully...");
	}

	@Override
	public List<Tweet> getAllTweets() {
		// producerService.sendMessage("Received request to send all tweet data.");
		logger.info("Retriving all the tweet data");
		return tweetRepository.findAll();
	}

	@Override
	public List<Tweet> getAllTweetsByUsername(String username) {
		logger.info("Retriving tweets of user: " + username);
		return tweetRepository.findByUserUsername(username);
	}

	@Override
	public Tweet postTweetByUsername(Tweet tweet, String username) {
		User user = userRepository.findByUsername(username);
		tweet.setUser(user);
		// producerService.sendMessage("Tweet posted by the user : " + username); // check here  to un comment it

		logger.info("Tweet posted by user: " + username);
		List<Tweet> li = tweetRepository.findAll();
		int count = li.size();
		int count1 = 0;
		for (Tweet t : li) {
			if (t.getReplies() == null)
				count1 = 0;
			else
				count += t.getReplies().size();

		}

		tweet.setId(Integer.toString(count + 1));
		LocalDateTime now = LocalDateTime.now();
		tweet.setPostDate(now);
		return tweetRepository.save(tweet);

	}

	@Override
	public void deleteTweetById(String username, String tweetId) {
		User user = userRepository.findByUsername(username);
		Tweet tweet = tweetRepository.findById(tweetId).get();
		String userNameOfTweetCreator = tweet.getUser().getUsername();

		if (username.equals(userNameOfTweetCreator)) {
			tweetRepository.deleteById(tweetId);

			logger.info("Deleted thw tweet for the tweet id" + tweetId);
		} else
			logger.info("Unable to delete tweet for the tweet id" + tweetId);
	}

	public Tweet getTweetById(String id) {
		return tweetRepository.findById(id).get();

	}

	@Override
	public Tweet replyTweetById(Tweet replyTweet, String parentTweetId, String username)
			throws IncorrectOrDeletedTweet {
		Optional<Tweet> parentTweet = tweetRepository.findById(parentTweetId);
		User user = userRepository.findByUsername(username);
		if (user != null) {
			replyTweet.setUser(user);
		}
		List<Tweet> li = tweetRepository.findAll();
		int count = li.size();
		int count1 = 0;
		for (Tweet t : li) {
			if (t.getReplies() == null)
				count1 = 0;
			else
				count += t.getReplies().size();

		}

		replyTweet.setId(Integer.toString(count + 1));
		LocalDateTime now = LocalDateTime.now();
		replyTweet.setPostDate(now);

		ArrayList<Tweet> replies = new ArrayList<Tweet>();
		if (parentTweet.isPresent()) {
			if (parentTweet.get().getReplies() != null) {
				replies = (ArrayList<Tweet>) parentTweet.get().getReplies();
				replies.add(replyTweet);
			} else {

				replies.add(replyTweet);
			}
			parentTweet.get().setReplies(replies);

			tweetRepository.save(parentTweet.get());
		} else {
			throw new IncorrectOrDeletedTweet("Incorrect or deleted parent tweet id.");
		}
		return replyTweet;

	}

	@Override
	public String likeTweetById(String username, String tweetId) {
		Tweet tweet = tweetRepository.findById(tweetId).orElse(null);
		if(tweet != null) {
			ArrayList<String> likes = tweet.getLike();
			if(likes.stream().filter(u-> u.equals(username)).toList().size()==0) {
				tweet.getLike().add(username);
				tweet.setLikes(tweet.getLikes()+1);

				tweetRepository.save(tweet);
				return null;
			}else
				return "You have already liked!";
		}else
			return "Tweet doesn't exist!";
	}
// 	@Override
// 	public void likeTweetById(String username, String tweetId) {

// 		Optional<Likes> tweetInLikeRepo = likeRepository.findByTweetId(tweetId);
// 		System.out.println(tweetInLikeRepo);
// 		if (!tweetInLikeRepo.isPresent()) {
// //			List<String> alreadyLikedUsers=new ArrayList<String>();

// 			Likes like = new Likes();
// 			like.setTweetId(tweetId);
// 			like.setId(likeRepository.findAll().size() + 1);
// 			List likeUsername = new ArrayList<String>();
// 			likeUsername.add(username);
// 			like.setUserName(likeUsername);


// 			// if(like.getUserName().stream().filter(u-> u.equals(username)).toList().size()==0)
// 			// 	like.getUserName().add(username);
// 			likeRepository.save(like);

// 			Optional<Tweet> tweet = tweetRepository.findById(tweetId);
// 			logger.info("Liked Tweet with Id: {} is {}", tweetId, tweet.get());
// 			if (tweet.isPresent()) {
// 				tweet.get().setLikes(tweet.get().getLikes() + 1);
// 				tweetRepository.save(tweet.get());

// 			}
// 		}

// 		if (tweetInLikeRepo.isPresent()) {
// //			List<String> alreadyLikedUsers=tweetInLikeRepo.get().getUserName();

// 			List<Likes> tweets = likeRepository.findAll();
// 			List<String> alreadyLikedUsers = new ArrayList<String>();
// 			for (Likes l : tweets) {
// 				if (l.getTweetId().equals(tweetId)) {
// 					alreadyLikedUsers = l.getUserName();
// 				}

// 			}
// //			System.out.println(alreadyLikedUsers);

// 			if (alreadyLikedUsers.contains(username)) {
// 				logger.info("Tweet has been already liked by the user");
// 				System.out.println("Tweet has been already liked by the user");
// 				return;
// 			} else {
// 				Likes like = new Likes();
// 				like.setTweetId(tweetId);
// 				like.setId(likeRepository.findByTweetId(tweetId).get().getId());

// 				alreadyLikedUsers.add(username);
// 				like.setUserName(alreadyLikedUsers);
// 				likeRepository.save(like);

// 				Optional<Tweet> tweet = tweetRepository.findById(tweetId);
// 				logger.info("Liked Tweet with Id: {} is {}", tweetId, tweet.get());
// 				if (tweet.isPresent()) {
// 					tweet.get().setLikes(tweet.get().getLikes() + 1);
// 					tweetRepository.save(tweet.get());
// 				}

// 			}
// 		}

// 	}
}
