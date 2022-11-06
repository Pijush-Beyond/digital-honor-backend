package com.tweetapp.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidUsernameOrPasswordException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidUsernameOrPasswordException(String msg) {
		super(msg);
	}

}
