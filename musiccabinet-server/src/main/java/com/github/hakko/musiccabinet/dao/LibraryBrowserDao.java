package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.LibraryStatistics;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface LibraryBrowserDao {

	boolean hasArtists();
	List<Artist> getArtists();
	Artist getArtist(String artistName);
	List<Artist> getArtists(int indexLetter);
	List<Artist> getArtists(String tag, int threshold);
	List<ArtistRecommendation> getRecentlyPlayedArtists(String lastFmUsername, boolean onlyAlbumArtists, int offset, int limit, String query);
	List<ArtistRecommendation> getMostPlayedArtists(String lastFmUsername, int offset, int limit, String query);
	List<ArtistRecommendation> getRandomArtists(boolean onlyAlbumArtists, int limit);
	List<ArtistRecommendation> getStarredArtists(String lastFmUsername, int offset, int limit, String query);

	Album getAlbum(Uri albumUri);
	void getAlbums(List<Album> albums, Artist artist, boolean sortAscending);
	void getAlbums(List<Album> albums, Artist artist, boolean sortByYear, boolean sortAscending);
	
	List<Album> getVariousArtistsAlbums();
	List<Album> getRecentlyAddedAlbums(int offset, int limit, String query);
	List<Album> getRecentlyPlayedAlbums(String lastFmUsername, int offset, int limit, String query);
	List<Album> getMostPlayedAlbums(String lastFmUsername, int offset, int limit, String query);
	List<Album> getRandomAlbums(int limit);
	List<Album> getStarredAlbums(String lastFmUsername, int offset, int limit, String query);
	List<Album> getAlbumsByName(int offset, int limit, String query);
	List<Album> getAlbumsByArtist(int offset, int limit, String query);
	List<Album> getAlbumsByYear(int offset, int limit, String query, int fromYear, int toYear);
	List<Album> getAlbumsByGenre(int offset, int limit, String query, String genre);
	
	Track getTrack(Uri trackUri);
	List<Track> getTracks(List<? extends Uri> trackUris);
	List<Track> getTracksByGenre(String genre, int offset, int limit);
	List<? extends Uri> getRecentlyPlayedTrackUris(String lastFmUsername, int offset, int limit, String query);
	List<? extends Uri> getMostPlayedTrackUris(String lastFmUsername, int offset, int limit, String query);
	List<? extends Uri> getStarredTrackUris(String lastFmUsername, int offset, int limit, String query);
	List<? extends Uri> getRandomTrackUris(int limit);
	List<? extends Uri> getRandomTrackUris(int limit, Integer fromYear, Integer toYear, String genre);
	
	String getCoverArtFileForTrack(Uri uri);
	void addArtwork(List<Track> tracks);
	String getLyricsForTrack(Uri uri);
	String getLyricsForTrack(String artistName, String trackName);

	List<Integer> getArtistIndexes();
	LibraryStatistics getStatistics();
	
	Uri getTrackUri(String filename);
	Uri getTrackUri(File file);
	
	void markAllFilesForFullRescan();
	List<String> getFilesMissingMetadata();
	
}