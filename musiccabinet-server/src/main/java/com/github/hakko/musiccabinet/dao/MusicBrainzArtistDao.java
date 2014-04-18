package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.MBArtist;

public interface MusicBrainzArtistDao {

	void createArtists(List<MBArtist> artists);
	MBArtist getArtist(Uri artistUri);
	int getMissingAndOutdatedArtistsCount();
	List<Artist> getMissingArtists();
	List<MBArtist> getOutdatedArtists();
	
}