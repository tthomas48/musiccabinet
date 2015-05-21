package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.music.Album;

public interface RatingDao {
	public List<Album> getHighestRatedAlbums(final int offset, final int count);

	public Double getAverageRating(Uri uri);

	public Integer getRatingForUser(String username, Uri uri);

	public int getRatedAlbumCount(final String username);

	public void setRatingForUser(String username, Uri uri, Integer rating);
}
