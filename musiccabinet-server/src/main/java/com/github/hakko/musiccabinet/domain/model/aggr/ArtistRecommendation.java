package com.github.hakko.musiccabinet.domain.model.aggr;

import com.github.hakko.musiccabinet.configuration.Uri;

/*
 * ArtistRecommendation doesn't map to a single database table but is rather aggregated
 * from music.artist and music.artistinfo.
 */
public class ArtistRecommendation {

	private String artistName;
	private String imageUrl;
	private Uri artistUri;
	
	public ArtistRecommendation(String artistName, Uri artistUri) {
		this(artistName, null, artistUri);
	}

	public ArtistRecommendation(String artistName, String imageUrl, Uri artistUri) {
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.artistUri = artistUri;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Uri getArtistUri() {
		return artistUri;
	}

	public void setArtistUri(Uri artistUri) {
		this.artistUri = artistUri;
	}
	
}