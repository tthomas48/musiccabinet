package com.github.hakko.musiccabinet.service.spotify;

import java.util.List;
import java.io.File;

/*
 * Keeps track of common settings for spotify update services.
 */
public class SpotifySettingsService {

	private String cache;
	private String userName;
	private String password;
  private File spotifyKey;
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

	public String getSpotifyPassword() {
		return password;
	}

	public void setSpotifyPassword(String password) {
		this.password = password;
	}

	public File getSpotifyKey() {
		return spotifyKey;
	}

	public void setSpotifyKey(File spotifyKey) {
		this.spotifyKey = spotifyKey;
	}

}
