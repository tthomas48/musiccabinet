package com.github.hakko.musiccabinet.domain.model.music;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.configuration.Uri;

public class Album {

	protected Artist artist;
	protected String name;
	protected Integer year;
	protected String coverArtPath;
	protected boolean coverArtEmbedded;
	protected String coverArtURL;
	protected List<Uri> trackUris;
	protected Uri uri;
	protected Uri spotifyUri;
	protected Integer rating;
	
	public Album() {
		setArtist(new Artist());
	}

	public Album(String albumName) {
		setArtist(new Artist());
		setName(albumName);
	}

	public Album(Artist artist, String albumName) {
		setArtist(artist);
		setName(albumName);
	}
	
	public Album(String artistName, String albumName) {
		setArtist(new Artist(artistName));
		setName(albumName);
	}
	
	public Album(int artistId, String artistName, int id, String name, Integer year, String coverArtFile,
			boolean coverArtEmbedded, String coverArtURL, List<Uri> trackUris, Uri spotifyUri, Integer rating) {
		this(new SubsonicUri(artistId), artistName, new SubsonicUri(id), name, year, coverArtFile, coverArtEmbedded, coverArtURL, trackUris, spotifyUri, rating);
	}
	
	public Album(Uri artistUri, String artistName, Uri albumUri, String name, Integer year, String coverArtFile,
			boolean coverArtEmbedded, String coverArtURL, List<Uri> trackUris, Uri spotifyUri, Integer rating) {
		this.artist = new Artist(artistUri, artistName);
		this.uri = albumUri;
		this.name = name;
		this.year = year;
		this.coverArtPath = coverArtFile;
		this.coverArtEmbedded = coverArtEmbedded;
		this.coverArtURL = coverArtURL;
		this.trackUris = trackUris;
		this.spotifyUri = spotifyUri;
		this.rating = rating;
	}
	
	
	public Album(Artist artist, int albumId, String name) {
		this.artist = artist;
		this.uri = new SubsonicUri(albumId);
		this.name = name;
	}
	
	public Album(Artist artist, Uri albumUri, String name) {
		this.artist = artist;
		this.uri = albumUri;
		this.name = name;
	}
	
	
	public Artist getArtist() {
		return artist;
	}

	public final void setArtist(Artist artist) {
		if (artist == null) {
			throw new IllegalArgumentException("Album artist cannot be set to null.");
		}
		this.artist = artist;
	}
	
	@Deprecated
	public int getId() {
		return uri.getId();
	}
	
	public Integer getYear() {
		return year;
	}
	
	public String getCoverArtPath() {
		return coverArtPath;
	}
	
	public void setCoverArtPath(String coverArtPath) {
		this.coverArtPath = coverArtPath;
	}

	public boolean isCoverArtEmbedded() {
		return coverArtEmbedded;
	}

	public String getCoverArtURL() {
		return coverArtURL;
	}
	
	public void setCoverArtURL(String coverArtURL) {
		this.coverArtURL = coverArtURL;
	}

	public List<Uri> getTrackUris() {
		return trackUris;
	}
	
	public void setTrackUris(List<Uri> trackUris) {
		this.trackUris = trackUris;
	}
	
	public String getName() {
		return name;
	}
	
	public final void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Album name cannot be set to null.");
		}
		this.name = name;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(artist)
		.append(name)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  Album t = (Album) o;
          return new EqualsBuilder()
          .append(artist, t.artist)
          .append(name, t.name)
          .isEquals();
	}
	
	@Override
	public String toString() {
		return "album " + name + " by " + artist;
	}
	
	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}
	
	public Uri getArtistUri() {
		return artist.getUri();
	}

	public Uri getSpotifyUri() {
		return spotifyUri;
	}

	public void setSpotifyUri(Uri spotifyUri) {
		this.spotifyUri = spotifyUri;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

}