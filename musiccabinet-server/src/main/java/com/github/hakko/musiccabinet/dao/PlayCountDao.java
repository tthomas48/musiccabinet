package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface PlayCountDao {

	void addPlayCount(LastFmUser lastFmUser, Track track);

	List<? extends Uri> getRecentArtists(String lastFmUser, int offset, int limit);
	List<? extends Uri> getRecentAlbums(String lastFmUser, int offset, int limit);
	List<? extends Uri> getRecentTracks(String lastFmUser, int offset, int limit);
	
	List<? extends Uri> getMostPlayedArtists(String lastFmUser, int offset, int limit);
	List<? extends Uri> getMostPlayedAlbums(String lastFmUser, int offset, int limit);
	List<? extends Uri> getMostPlayedTracks(String lastFmUser, int offset, int limit);
}