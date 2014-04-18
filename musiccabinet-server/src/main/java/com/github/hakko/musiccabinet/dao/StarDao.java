package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public interface StarDao {

	void starArtist(LastFmUser lastFmUser, Uri artistUri);
	void unstarArtist(LastFmUser lastFmUser, Uri artistUri);
	List<? extends Uri> getStarredArtistUris(LastFmUser lastFmUser, int offset, int limit, String query);

	void starAlbum(LastFmUser lastFmUser, Uri albumUri);
	void unstarAlbum(LastFmUser lastFmUser, Uri albumUri);
	List<? extends Uri> getStarredAlbumUris(LastFmUser lastFmUser, int offset, int limit, String query);

	void starTrack(LastFmUser lastFmUser, Uri trackUri);
	void unstarTrack(LastFmUser lastFmUser, Uri trackUri);
	List<? extends Uri> getStarredTrackUris(LastFmUser lastFmUser, int offset, int limit, String query);

}