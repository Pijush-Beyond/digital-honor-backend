package com.tweetapp.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tweetapp.api.exception.InvalidUsernameOrPasswordException;
import com.tweetapp.api.exception.UsernameAlreadyExists;
import com.tweetapp.api.kafka.ProducerService;
import com.tweetapp.api.model.ErrorMessages;
import com.tweetapp.api.model.Tweet;
import com.tweetapp.api.model.User;
import com.tweetapp.api.model.UserResponse;
import com.tweetapp.api.repository.UserRepository;
import com.tweetapp.api.service.TweetService;
import com.tweetapp.api.service.UserService;

// @CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1.0/tweets")

public class TweetAppController {

	@Autowired
	UserService userService;

	@Autowired
	TweetService tweetService;

	@Autowired
	ProducerService producerService;

	Logger logger = LoggerFactory.getLogger(TweetAppController.class);

	/*
	 * method to register the users in the tweet app
	 */
	@PostMapping("/register")
	public ResponseEntity<Object> registerUser(@RequestBody User user) throws UsernameAlreadyExists {
		try {
			ResponseEntity<Object> response = new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
			logger.info("User created successfully...");
			return response;
		} catch (UsernameAlreadyExists e) {
			throw new UsernameAlreadyExists(ErrorMessages.USER_ALREADY_EXISTS.getMessage());
		}

	}

	/*
	 * method to login the valid users into the tweet app
	 */
	@ResponseBody
	@PostMapping("/login")
	public ResponseEntity<UserResponse> loginUser(Model model, @RequestBody User user, HttpServletRequest request, HttpServletResponse response) // before: - HttpServletResponse response
			throws InvalidUsernameOrPasswordException {
		logger.debug("----Inside TweetAppController-> loginUser()");
		try {
			UserResponse authUser = userService.loginUser(user.getUsername(), user.getPassword());
			System.out.print(authUser);
			if (authUser != null) {
				request.getSession().setAttribute("user", user.getUsername());
				// Cookie authCookie = new Cookie("authCookie", authUser.getToken()+";SameSite=None");
				// authCookie.setPath("/");
				// // authCookie.setDomain(".amazonaws.com");
				// authCookie.setSecure(false);
				// response.addCookie(authCookie);
				// ResponseCookie cookie = ResponseCookie.from("authCookie", authUser.getToken())
				// 								.sameSite("None")
				// 								.path("/")
				// 								.secure(true)
				// 								.domain(request.getHeader(HttpHeaders.HOST).split(":")[0])
				// 								.build();
				// response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
				return new ResponseEntity<UserResponse>(authUser, HttpStatus.OK);
			} else {
				response.addCookie(new Cookie("authCookie", null)); // on unsucessfull login remove auth Cookie
				throw new InvalidUsernameOrPasswordException(ErrorMessages.INVALID_CREDENTIALS.getMessage());
			}
		} catch (InvalidUsernameOrPasswordException e) {
			response.addCookie(new Cookie("authCookie", null)); // on unsucessfull login remove auth Cookie
			throw new InvalidUsernameOrPasswordException(ErrorMessages.INVALID_CREDENTIALS.getMessage());
		}
	}

	/*
	 * redirects to forgot password page...
	 */
	@ResponseBody
	@GetMapping("/{username}/forgot")
	public Map<String, String> forgotPassword(@PathVariable("username") String username) {
		logger.info("Forgot Password request received with username: " + username);
		return new HashMap<String, String>(userService.forgotPassword(username));

	}

	/*
	 * redirects to method to reset the password for the logged in users
	 */
	@ResponseBody
	@PostMapping("/reset")
	public Map<String, String> resetUserPassword(@RequestBody User user) {
		logger.info("Request to reset the password");
		return new HashMap<String, String>(userService.resetPassword(user.getUsername(), user.getPassword()));

	}

	/*
	 * redirects to the method that retreives all the tweets
	 */
	@GetMapping("/all")
	public ResponseEntity<List<Tweet>> getAllTweets() {
		logger.info("Retreive all the tweets by the all users");
		return new ResponseEntity<>(tweetService.getAllTweets(), HttpStatus.OK);
	}

	/*
	 * redirects to the method to retrive all the users
	 */
	@GetMapping("/users/all")
	public ResponseEntity<List<User>> getAllUsers() {
		logger.info("Retrived all the users");
		return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
	}

	/*
	 * redirects to the method to search user by the username
	 */
	@GetMapping("/user/search/{username}")
	public ResponseEntity<List<User>> searchUser(@PathVariable("username") String username)
			throws InvalidUsernameOrPasswordException {
		logger.info("Retriving the user by the username");
		return new ResponseEntity<>(userService.getUserByUsername(username), HttpStatus.OK);
	}

	/*
	 * redirects to the method to get all the tweets for a respective user
	 */
	@GetMapping("/{username}")
	public ResponseEntity<List<Tweet>> getAllTweetsByUser(@PathVariable("username") String username) {
		logger.info("Retriving all the tweets by the user");
		return new ResponseEntity<>(tweetService.getAllTweetsByUsername(username), HttpStatus.OK);
	}

	/*
	 * redirects to the method to post tweet by the user
	 */
	@PostMapping("/{username}/add")
	public ResponseEntity<Tweet> postTweetByUser(@PathVariable("username") String username, @RequestBody Tweet tweet) {
		logger.info("Tweet successfully posted by the user");
		return new ResponseEntity<>(tweetService.postTweetByUsername(tweet, username), HttpStatus.OK);
	}

	/*
	 * redirects to the method to update the tweet
	 */
	@PutMapping("/{username}/update/{id}")
	public ResponseEntity<Tweet> updateTweetByUser(@PathVariable("username") String username,
			@PathVariable("id") String tweetId, @RequestBody Tweet tweet) {
		logger.info("Tweet successfully updated by the user");
		return new ResponseEntity<>(tweetService.editTweet(tweet, tweetId), HttpStatus.OK);
	}

	/*
	 * redirects to the method of deleting the tweet by the user
	 */
	@DeleteMapping("/{username}/delete/{id}")
	public ResponseEntity<HttpStatus> deleteTweetByUser(@PathVariable("username") String username,
			@PathVariable("id") String tweetId) {

		tweetService.deleteTweetById(username, tweetId);
		logger.info("Tweet deleted by the user");
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/*
	 * redirects to the method to like post
	 */
	@GetMapping("/{username}/like/{id}")
	public ResponseEntity<String> likeTweetByUser(@PathVariable("username") String username,
			@PathVariable("id") String tweetId) {
		System.out.println("\n\nn\n\n>>>>>\n\n\n");
		String result = tweetService.likeTweetById(username, tweetId);
		if(result == null){
			logger.info("Like a tweet ");
			return new ResponseEntity<String>(HttpStatus.OK);
		}else
			return new ResponseEntity<String>(result,HttpStatus.BAD_REQUEST);
	}

//	used to get tweet by tweet id
	@GetMapping("/tweet/{id}")
	public Tweet getTweetById(@PathVariable("id") String id) {

		return tweetService.getTweetById(id);

	}

	/*
	 * redirects to the method to reply to a tweet
	 */
	@PostMapping("/{username}/reply/{id}")
	public ResponseEntity<Tweet> replyTweetByUser(@PathVariable("username") String username,
			@PathVariable("id") String tweetId, @RequestBody Tweet replyTweet) {

		try {
			logger.info("Replying to the tweet by user");
			return new ResponseEntity<>(tweetService.replyTweetById(replyTweet, tweetId, username), HttpStatus.OK);
		} catch (Exception e) {

			e.printStackTrace();
			return new ResponseEntity<>(new Tweet(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
