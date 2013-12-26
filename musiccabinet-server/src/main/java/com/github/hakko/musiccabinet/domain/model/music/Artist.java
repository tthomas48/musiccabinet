package com.github.hakko.musiccabinet.domain.model.music;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Artist implements Comparable<Artist> {

	private int id;
	private String name;
	private String sortName;
	private URI uri;

	public Artist() {

	}

	public Artist(String name) {
		setName(name);
	}

	public Artist(int id, String name) {
		this.id = id;
		this.name = name;
		try {
			this.uri = new URI("subsonic:" + id);
		} catch (URISyntaxException e) {
			
		}
	}
	
	public Artist(String uri, String name) {
		this.id = -1;
		if(uri != null) {
			try {
				this.uri = new URI(uri);
			} catch (Exception e) {
			}
		} else {
			System.err.println("URI is null");
		}
		this.name = name;
	}

	@Deprecated
	public int getId() {
		return id;
	}

	@Deprecated
	public void setId(int id) {
		this.id = id;
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
	
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
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

}