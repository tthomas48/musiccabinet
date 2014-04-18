package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;

public interface ArtistRecommendationDao {

	List<ArtistRecommendation> getRelatedArtistsInLibrary(Uri artistUri, int amount, boolean onlyAlbumArtists);
	List<String> getRelatedArtistsNotInLibrary(Uri artistUri, int amount, boolean onlyAlbumArtists);

	List<ArtistRecommendation> getGenreArtistsInLibrary(String tagName, int offset, int length, boolean onlyAlbumArtists);
	List<String> getGenreArtistsNotInLibrary(String tagName, int amount, boolean onlyAlbumArtists);

	List<ArtistRecommendation> getGroupArtistsInLibrary(String lastFmGroupName, int offset, int length, boolean onlyAlbumArtists);
	List<String> getGroupArtistsNotInLibrary(String lastFmGroupName, int amount, boolean onlyAlbumArtists);

	List<ArtistRecommendation> getRecommendedArtistsInLibrary(String lastFmUsername, int offset, int length, boolean onlyAlbumArtists);
	List<String> getRecommendedArtistsNotInLibrary(String lastFmUsername, int amount, boolean onlyAlbumArtists);

}