package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;

public interface PlaylistGeneratorDao {

	// These methods assume a populated database.
	// Appropriate ws calls should be made beforehand.
	
	List<? extends Uri> getTopTracksForArtist(Uri artistUri, int totalCount);
	List<PlaylistItem> getPlaylistForTrack(Uri trackUri);
	List<PlaylistItem> getPlaylistForArtist(Uri artistUri, int artistCount, int totalCount);
	List<PlaylistItem> getPlaylistForTags(String[] tags, int artistCount, int totalCount);
	List<PlaylistItem> getPlaylistForGroup(String lastFmGroup, int artistCount, int totalCount);
	List<? extends Uri> getPlaylistForRelatedArtists(Uri artistUri, int artistCount, int totalCount);

	void setAllowedTrackLengthInterval(int minLength, int maxLength);
	void updateSearchIndex();
	boolean isSearchIndexCreated();
	
}
