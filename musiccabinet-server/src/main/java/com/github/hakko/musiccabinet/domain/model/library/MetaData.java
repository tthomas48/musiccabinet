package com.github.hakko.musiccabinet.domain.model.library;

import static org.apache.commons.lang.math.NumberUtils.isDigits;
import static org.apache.commons.lang.math.NumberUtils.toInt;

import com.github.hakko.musiccabinet.configuration.Uri;

public class MetaData {

	private Mediatype mediaType;
	private Short bitrate;
	private Boolean vbr;
	private Short duration;

	private Uri uri;
	private String artist;
	private Uri artistUri;
	private String albumArtist;
	private String composer;
	private String album;
	private Uri albumUri;
	private String title;
	private Short trackNr;
	private Short trackNrs;
	private Short discNr;
	private Short discNrs;
	private Integer year;
	private String genre;
	private String lyrics;
	private Boolean hasLyrics;
	private Boolean isCoverArtEmbedded;
	private String path;
	private String artworkPath;
	private Long size;
	private Long modified;
	private Integer explicit;

	private String artistSort;
	private String albumArtistSort;

	private Integer width;
	private Integer height;

	public Mediatype getMediaType() {
		return mediaType;
	}

	public void setMediaType(Mediatype mediaType) {
		this.mediaType = mediaType;
	}

	public Short getBitrate() {
		return bitrate;
	}

	public void setBitrate(Short bitrate) {
		this.bitrate = bitrate;
	}

	public Boolean isVbr() {
		if(vbr == null) {
			return Boolean.FALSE;
		}
		return vbr;
	}

	public void setVbr(Boolean vbr) {
		this.vbr = vbr;
	}

	public Short getDuration() {
		return duration;
	}

	public String getDurationAsString() {
		if (duration == null) {
			return null;
		}

		StringBuffer result = new StringBuffer(8);

		int seconds = duration;

		int hours = seconds / 3600;
		seconds -= hours * 3600;

		int minutes = seconds / 60;
		seconds -= minutes * 60;

		if (hours > 0) {
			result.append(hours).append(':');
			if (minutes < 10) {
				result.append('0');
			}
		}

		result.append(minutes).append(':');
		if (seconds < 10) {
			result.append('0');
		}
		result.append(seconds);

		return result.toString();
	}

	public void setDuration(Short duration) {
		this.duration = duration;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public Uri getArtistUri() {
		return artistUri;
	}

	public void setArtistUri(Uri artistUri) {
		this.artistUri = artistUri;
	}

	public String getAlbumArtist() {
		return albumArtist;
	}

	public void setAlbumArtist(String albumArtist) {
		this.albumArtist = albumArtist;
	}

	public String getComposer() {
		return composer;
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public void setAlbumUri(Uri albumUri) {
		this.albumUri = albumUri;
	}

	public Uri getAlbumUri() {
		return this.albumUri;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Short getTrackNr() {
		return trackNr;
	}

	public void setTrackNr(Short trackNr) {
		this.trackNr = trackNr;
	}

	public Short getTrackNrs() {
		return trackNrs;
	}

	public void setTrackNrs(Short trackNrs) {
		this.trackNrs = trackNrs;
	}

	public Short getDiscNr() {
		return discNr;
	}

	public void setDiscNr(Short discNr) {
		this.discNr = discNr;
	}

	public Short getDiscNrs() {
		return discNrs;
	}

	public void setDiscNrs(Short discNrs) {
		this.discNrs = discNrs;
	}

	public Integer getYear() {
		return year;
	}

	public String getYearAsString() {
		if (year == null) {
			return "";
		}
		return year.toString();
	}

	public void setYear(String year) {
		if (year != null && year.length() > 4) {
			year = year.substring(0, 4);
		}
		this.year = isDigits(year) && toInt(year) > 1500 ? toInt(year) : null;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getLyrics() {
		return lyrics;
	}

	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}

	public boolean hasLyrics() {
		return hasLyrics;
	}

	public void setHasLyrics(boolean hasLyrics) {
		this.hasLyrics = hasLyrics;
	}

	public Boolean isCoverArtEmbedded() {
		if (isCoverArtEmbedded == null) {
			return Boolean.FALSE;
		}
		return isCoverArtEmbedded;
	}

	public void setCoverArtEmbedded(Boolean isCoverArtEmbedded) {
		this.isCoverArtEmbedded = isCoverArtEmbedded;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getArtworkPath() {
		return artworkPath;
	}

	public void setArtworkPath(String artworkPath) {
		this.artworkPath = artworkPath;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public Long getModified() {
		return modified;
	}

	public void setModified(Long modified) {
		this.modified = modified;
	}

	public String getArtistSort() {
		return artistSort;
	}

	public void setArtistSort(String sortArtist) {
		this.artistSort = sortArtist;
	}

	public String getAlbumArtistSort() {
		return albumArtistSort;
	}

	public void setAlbumArtistSort(String albumArtistSort) {
		this.albumArtistSort = albumArtistSort;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public enum Mediatype {

		// these are taken from org.jaudiotagger.audio.SupportedFileFormat,
		// and map to library.fileheader_type

		OGG("OGG"), MP3("MP3"), FLAC("FLAC"), MP4("MP4"), M4A("M4A"), M4P("M4P"), WMA(
				"WMA"), WAV("WAV"), RA("RA"), RM("RM"), M4B("M4B");

		private String filesuffix;

		Mediatype(String filesuffix) {
			this.filesuffix = filesuffix;
		}

		public String getFilesuffix() {
			return filesuffix;
		}

	}
	
	public void setMediaType(String filesuffix) {
		
		this.mediaType = Mediatype.valueOf(filesuffix.toUpperCase());
	}

	public Integer getExplicit() {
		return explicit;
	}

	public void setExplicit(Integer explicit) {
		this.explicit = explicit;
	}

	public boolean isExplicit() {
		return explicit == 1;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

}