package com.github.hakko.musiccabinet.service.spotify;

/*
 * Keeps track of common settings for spotify update services.
 */
public class SpotifySettingsService {

	private String cache;
	private String userName;

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

}
