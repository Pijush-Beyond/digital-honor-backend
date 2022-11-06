package com.tweetapp.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
//@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
public class TweetApplication {

	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(TweetApplication.class);
		logger.debug("===========> Tweet App Has Been Started <===========");
		SpringApplication.run(TweetApplication.class, args);
	}
}
