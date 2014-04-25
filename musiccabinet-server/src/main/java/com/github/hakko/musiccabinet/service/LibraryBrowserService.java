package com.github.hakko.musiccabinet.service;

import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.LibraryStatistics;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

/*
 */
public class LibraryBrowserService {

	protected LibraryBrowserDao libraryBrowserDao;
	
	public boolean hasArtists() {
		return libraryBrowserDao.hasArtists();
	}
	
	public List<Artist> getArtists() {
		return libraryBrowserDao.getArtists();
	}
	
	public List<Artist> getArtists(int indexLetter) {
		return libraryBrowserDao.getArtists(indexLetter);
	}

	public List<Artist> getArtists(String tag, int treshold) {
		return libraryBrowserDao.getArtists(tag, treshold);
	}
	
	public List<ArtistRecommendation> getRecentlyPlayedArtists(String lastFmUsername, boolean onlyAlbumArtists, int offset, int limit, String query) {
		return libraryBrowserDao.getRecentlyPlayedArtists(lastFmUsername, onlyAlbumArtists, offset, limit, query);
	}

	public List<ArtistRecommendation> getMostPlayedArtists(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getMostPlayedArtists(lastFmUsername, offset, limit, query);
	}

	public List<ArtistRecommendation> getRandomArtists(boolean onlyAlbumArtists, int limit) {
		return libraryBrowserDao.getRandomArtists(onlyAlbumArtists, limit);
	}

	public List<ArtistRecommendation> getStarredArtists(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getStarredArtists(lastFmUsername, offset, limit, query);
	}

	public Album getAlbum(Uri albumUri) {
		return libraryBrowserDao.getAlbum(albumUri);
	}

	public List<Album> getAlbums(Artist artist, boolean sortAscending) {
		return getAlbums(artist, true, sortAscending);
	}

	public List<Album> getAlbums(Artist artist, boolean sortByYear, boolean sortAscending) {
		List<Album> albums = new ArrayList<Album>();
		libraryBrowserDao.getAlbums(albums, artist, sortByYear, sortAscending);
		return albums;
	}

	public List<Album> getVariousArtistsAlbums() {
		return libraryBrowserDao.getVariousArtistsAlbums();
	}
	
	public List<Album> getRecentlyAddedAlbums(int offset, int limit, String query) {
		return libraryBrowserDao.getRecentlyAddedAlbums(offset, limit, query);
	}

	public List<Album> getRecentlyPlayedAlbums(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getRecentlyPlayedAlbums(lastFmUsername, offset, limit, query);
	}

	public List<Album> getMostPlayedAlbums(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getMostPlayedAlbums(lastFmUsername, offset, limit, query);
	}

	public List<Album> getRandomAlbums(int limit) {
		return libraryBrowserDao.getRandomAlbums(limit);
	}

	public List<Album> getStarredAlbums(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getStarredAlbums(lastFmUsername, offset, limit, query);
	}

	public List<Track> getTracks(List<? extends Uri> trackUris) {
		return libraryBrowserDao.getTracks(trackUris);
	}
	
	public List<? extends Uri> getRecentlyPlayedTrackUris(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getRecentlyPlayedTrackUris(lastFmUsername, offset, limit, query);
	}

	public List<? extends Uri> getMostPlayedTrackUris(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getMostPlayedTrackUris(lastFmUsername, offset, limit, query);
	}

	public List<? extends Uri> getStarredTrackUris(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getStarredTrackUris(lastFmUsername, offset, limit, query);
	}

	public List<? extends Uri> getRandomTrackUris(int limit) {
		return libraryBrowserDao.getRandomTrackUris(limit);
	}

	public List<? extends Uri> getRandomTrackUris(int limit, Integer fromYear, Integer toYear, String genre) {
		return libraryBrowserDao.getRandomTrackUris(limit, fromYear, toYear, genre);
	}

	public String getCoverArtFileForTrack(Uri trackUri) {
		return libraryBrowserDao.getCoverArtFileForTrack(trackUri);
	}

	public void addArtwork(List<Track> tracks) {
		libraryBrowserDao.addArtwork(tracks);
	}
	
	public String getLyricsForTrack(Uri trackUri) {
		return libraryBrowserDao.getLyricsForTrack(trackUri);
	}

	public String getLyricsForTrack(String artistName, String trackName) {
		return libraryBrowserDao.getLyricsForTrack(artistName, trackName);
	}

	public List<Integer> getArtistIndexes() {
		return libraryBrowserDao.getArtistIndexes();
	}
	
	public LibraryStatistics getStatistics() {
		return libraryBrowserDao.getStatistics();
	}
	
	public Uri getTrackUri(String filename) {
		return libraryBrowserDao.getTrackUri(filename);
	}

	public void markAllFilesForFullRescan() {
		libraryBrowserDao.markAllFilesForFullRescan();
	}

	public List<String> getFilesMissingMetadata() {
		return libraryBrowserDao.getFilesMissingMetadata();
	}

	// Spring setters

	public void setLibraryBrowserDao(LibraryBrowserDao libraryBrowserDao) {
		this.libraryBrowserDao = libraryBrowserDao;
	}
	
}