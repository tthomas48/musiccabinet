package com.github.hakko.musiccabinet.service.spotify;

import java.util.List;

/*
 * Keeps track of common settings for spotify update services.
 */
public class SpotifySettingsService {

	private String cache;
	private String userName;
	private List<String> spotifyUsers;

	public String getSpotifyCache() {
		return cache;
	}

	public void setSpotifyCache(String spotifyCache) {
		cache = spotifyCache;
	}

	public String getSpotifyUserName() {
		return userName;
	}

	public void setSpotifyUserName(String userName) {
		this.userName = userName;
	}

	public List<String> getSpotifyUsers() {
		return spotifyUsers;
	}

	public void setSpotifyUsers(List<String> spotifyUsers) {
		this.spotifyUsers = spotifyUsers;
	}

}
