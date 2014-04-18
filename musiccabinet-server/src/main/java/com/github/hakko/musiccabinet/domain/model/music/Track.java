package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;

public class Track implements Comparable<Track> {

	private Uri uri;
	private Artist artist;
	private String name;
	private MetaData metaData;

	public Track() {
		setArtist(new Artist());
	}

	public Track(Artist artist, String trackName) {
		setArtist(artist);
		setName(trackName);
	}

	public Track(Artist artist, int trackId, String trackName) {
		setArtist(artist);
		setName(trackName);
		this.uri = new SubsonicUri(trackId);
	}

	public Track(String artistName, String trackName) {
		setArtist(new Artist(artistName));
		setName(trackName);
	}
	
	public Track(Uri uri, String trackName, MetaData metaData) {
		this.uri = uri;
		this.artist = new Artist(metaData.getArtist());
		this.name = trackName;
		this.metaData = metaData;
	}

	public Track(int trackId, String trackName, MetaData metaData) {
		this(new SubsonicUri(trackId), trackName, metaData);
	}

	@Deprecated
	public int getId() {
		return this.uri.getId();
	}

	public Artist getArtist() {
		return artist;
	}

	public final void setArtist(Artist artist) {
		if (artist == null) {
			throw new IllegalArgumentException(
					"Track artist cannot be set to null.");
		}
		this.artist = artist;
	}

	public String getName() {
		return name;
	}

	public final void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException(
					"Track name cannot be set to null.");
		}
		this.name = name;
	}

	public MetaData getMetaData() {
		return metaData;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(artist).append(name).toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o.getClass() != getClass())
			return false;

		Track t = (Track) o;
		return new EqualsBuilder().append(artist, t.artist)
				.append(name, t.name).isEquals();
	}

	@Override
	public int compareTo(Track t) {
		return new CompareToBuilder().append(artist, t.artist)
				.append(name, t.name).toComparison();
	}

	@Override
	public String toString() {
		return "track " + name + " by " + artist;
	}

	public Uri getUri() {
		return this.uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

}