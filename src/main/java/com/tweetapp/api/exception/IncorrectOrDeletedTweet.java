package com.tweetapp.api.exception;

public class IncorrectOrDeletedTweet extends RuntimeException {

	/**
	 * IncorrectOrDeletedTweet Exception
	 */
	private static final long serialVersionUID = 1L;

	public IncorrectOrDeletedTweet(String msg) {

		super(msg);
	}
}
