package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.util.URIUtil;

public class Artist implements Comparable<Artist> {

	private String name;
	private String sortName;
	private Uri uri;
	private Uri spotifyUri;

	public Artist() {

	}

	public Artist(String name) {
		setName(name);
	}

	public Artist(int id, String name) {
		this.name = name;
		this.uri = new SubsonicUri(id);
	}

	public Artist(String uri, String name) {
		this.uri = URIUtil.parseURI(uri);
		this.name = name;
	}
	
	public Artist(Uri uri, String name) {
		this.uri = uri;
		this.name = name;
	}


	@Deprecated
	public int getId() {
		return uri.getId();
	}

	public void setId(int id) {
		this.uri = new SubsonicUri(id);
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException(
					"Artist name cannot be set to null.");
		}
		this.name = name;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o.getClass() != getClass())
			return false;

		Artist a = (Artist) o;
		return new EqualsBuilder().append(name, a.name).isEquals();
	}

	@Override
	public int compareTo(Artist a) {
		return name.compareTo(a.name);
	}

	@Override
	public String toString() {
		return "artist " + name;
	}

	public Uri getSpotifyUri() {
		return spotifyUri;
	}

	public void setSpotifyUri(Uri spotifyUri) {
		this.spotifyUri = spotifyUri;
	}

}