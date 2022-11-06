package com.tweetapp.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tweetapp.api.model.Likes;

public interface LikeRepository extends MongoRepository<Likes, Integer> {

	Optional<Likes> findByTweetId(String tweetId);

}
