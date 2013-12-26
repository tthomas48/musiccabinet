package com.github.hakko.musiccabinet.dao;

import com.github.hakko.musiccabinet.domain.model.library.SpotifyUser;

public interface SpotifyDao {

	int getSpotifyUserId(String spotifyUsername);

	SpotifyUser getSpotifyUser(String spotifyUsername);

	void createOrUpdateSpotifyUser(SpotifyUser spotifyUser);
}