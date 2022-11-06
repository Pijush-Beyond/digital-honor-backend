package com.tweetapp.api.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tweetlikes")
public class Likes {

	@Id
	private Integer id;
	private String tweetId;

	private List<String> userName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTweetId() {
		return tweetId;
	}

	public void setTweetId(String tweetId) {
		this.tweetId = tweetId;
	}

	public List<String> getUserName() {
		return userName;
	}

	public void setUserName(List<String> userName) {
		this.userName = userName;
	}

	public Likes(Integer id, String tweetId, List<String> userName) {
		super();
		this.id = id;
		this.tweetId = tweetId;
		this.userName = userName;
	}

	public Likes() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "Likes [id=" + id + ", tweetId=" + tweetId + ", userName=" + userName + "]";
	}

}
