package com.github.hakko.musiccabinet.domain.model.library;

import jahspotify.media.User;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class SpotifyUser {

	private int id = -1;
	private String userName;
	private String blob;
	private String fullName;
	private String displayName;
	private String country;
	private String imageURL;

	public SpotifyUser() {
	}

	public SpotifyUser(String userName) {
		this.userName = userName;
	}

	public SpotifyUser(String userName, String blob) {
		this.userName = userName;
		this.blob = blob;
	}
	
	public SpotifyUser(User user) {
		this.userName = user.getUserName();
		this.fullName = user.getFullName();
		this.displayName = user.getDisplayName();
		this.country = user.getCountry();
		if(user.getImageURL() != null) {
			this.imageURL = user.getImageURL().toString();
		}
	}

	public SpotifyUser(int id, String userName, String blob, String fullName,
			String displayName, String country, String imageURL) {
		this.id = id;
		this.userName = userName;
		this.blob = blob;
		this.fullName = fullName;
		this.displayName = displayName;
		this.country = country;
		this.imageURL = imageURL;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getBlob() {
		return blob;
	}

	public void setBlob(String blob) {
		this.blob = blob;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(userName).append(blob).toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o.getClass() != getClass())
			return false;

		SpotifyUser u = (SpotifyUser) o;
		return new EqualsBuilder().append(userName, u.userName)
				.append(blob, u.blob).isEquals();
	}

	@Override
	public String toString() {
		return "User " + userName;
	}

}