package com.github.hakko.musiccabinet.domain.model.music;

import com.github.hakko.musiccabinet.configuration.Uri;

/*
 * Artist representation from MusicBrainz.
 * 
 * Contains artist name, mbid (36 chars), and life-span (begin year, ended).
 */
public class MBArtist {

	private String name;
	private Uri uri;
	private String mbid;
	private String countryCode;
	private short startYear;
	private boolean active;

	public MBArtist() {
		
	}
	
	public MBArtist(String name, String mbid, String countryCode,
			short startYear, boolean active) {
		this.name = name;
		this.mbid = mbid;
		this.countryCode = countryCode;
		this.startYear = startYear;
		this.active = active;
	}

	public MBArtist(Uri uri, String name, String mbid, String countryCode,
			short startYear, boolean active) {
		this(name, mbid, countryCode, startYear, active);
		this.uri = uri;
	}
	
	public Uri getUri() {
		return uri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public short getStartYear() {
		return startYear;
	}

	public void setStartYear(short startYear) {
		this.startYear = startYear;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
}