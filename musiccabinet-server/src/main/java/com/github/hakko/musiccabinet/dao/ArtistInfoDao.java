package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;

public interface ArtistInfoDao {

	void createArtistInfo(List<ArtistInfo> artistInfo);
	ArtistInfo getArtistInfo(Uri uri);
	ArtistInfo getDetailedArtistInfo(Uri uri);
	ArtistInfo getArtistInfo(Artist artist);
	
	void setBioSummary(Uri artistUri, String biosummary);
}
