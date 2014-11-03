package com.github.hakko.musiccabinet.dao.aggregate;

import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.LibraryStatistics;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class AggregateLibraryBrowserDao implements LibraryBrowserDao {

	private List<LibraryBrowserDao> daos;

	public void setDaos(List<LibraryBrowserDao> daos) {
		this.daos = daos;
	}

	@Override
	public boolean hasArtists() {
		for (LibraryBrowserDao dao : daos) {
			if (dao.hasArtists()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Artist getArtist(String artistName) {
		for (LibraryBrowserDao dao : daos) {
			Artist artist = dao.getArtist(artistName);
			if(artist != null) {
				return artist;
			}
		}
		return null;
	}

	@Override
	public List<Artist> getArtists() {
		List<Artist> artists = new ArrayList<Artist>();
		for (LibraryBrowserDao dao : daos) {
			artists.addAll(dao.getArtists());
		}
		return artists;
	}

	@Override
	public List<Artist> getArtists(int indexLetter) {
		List<Artist> artists = new ArrayList<Artist>();
		for (LibraryBrowserDao dao : daos) {
			artists.addAll(dao.getArtists(indexLetter));
		}
		return artists;
	}

	@Override
	public List<Artist> getArtists(String tag, int threshold) {
		List<Artist> artists = new ArrayList<Artist>();
		for (LibraryBrowserDao dao : daos) {
			artists.addAll(dao.getArtists(tag, threshold));
		}
		return artists;
	}

	@Override
	public List<ArtistRecommendation> getRecentlyPlayedArtists(
			String lastFmUsername, boolean onlyAlbumArtists, int offset,
			int limit, String query) {
		List<ArtistRecommendation> artists = new ArrayList<ArtistRecommendation>();
		for (LibraryBrowserDao dao : daos) {
			artists.addAll(dao.getRecentlyPlayedArtists(lastFmUsername,
					onlyAlbumArtists, offset, limit, query));
		}
		return artists;
	}

	@Override
	public List<ArtistRecommendation> getMostPlayedArtists(
			String lastFmUsername, int offset, int limit, String query) {
		List<ArtistRecommendation> artists = new ArrayList<ArtistRecommendation>();
		for (LibraryBrowserDao dao : daos) {
			artists.addAll(dao.getMostPlayedArtists(lastFmUsername, offset,
					limit, query));
		}
		return artists;
	}

	@Override
	public List<ArtistRecommendation> getRandomArtists(
			boolean onlyAlbumArtists, int limit) {
		List<ArtistRecommendation> artists = new ArrayList<ArtistRecommendation>();
		for (LibraryBrowserDao dao : daos) {
			artists.addAll(dao.getRandomArtists(onlyAlbumArtists, limit));
		}
		return artists;
	}

	@Override
	public List<ArtistRecommendation> getStarredArtists(String lastFmUsername,
			int offset, int limit, String query) {
		List<ArtistRecommendation> artists = new ArrayList<ArtistRecommendation>();
		for (LibraryBrowserDao dao : daos) {
			artists.addAll(dao.getStarredArtists(lastFmUsername, offset, limit,
					query));
		}
		return artists;
	}

	@Override
	public Album getAlbum(Uri albumUri) {
		for (LibraryBrowserDao dao : daos) {
			Album album = dao.getAlbum(albumUri);
			if (album != null) {
				return album;
			}
		}
		return null;
	}

	@Override
	public void getAlbums(List<Album> albums, Artist artist, boolean sortAscending) {
		for (LibraryBrowserDao dao : daos) {
			dao.getAlbums(albums, artist, sortAscending);
		}
	}

	@Override
	public void getAlbums(List<Album> albums, Artist artist, boolean sortByYear,
			boolean sortAscending) {
		for (LibraryBrowserDao dao : daos) {
			dao.getAlbums(albums, artist, sortByYear, sortAscending);
		}
	}

	@Override
	public List<Album> getVariousArtistsAlbums() {
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getVariousArtistsAlbums());
		}
		return albums;
	}

	@Override
	public List<Album> getRecentlyAddedAlbums(boolean spotifyEnabled, int offset, int limit,
			String query) {
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getRecentlyAddedAlbums(spotifyEnabled, offset, limit, query));
		}
		return albums;
	}

	@Override
	public List<Album> getRecentlyPlayedAlbums(boolean spotifyEnabled, String lastFmUsername,
			int offset, int limit, String query) {
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getRecentlyPlayedAlbums(spotifyEnabled, lastFmUsername, offset,
					limit, query));
		}
		return albums;
	}

	@Override
	public List<Album> getMostPlayedAlbums(boolean spotifyEnabled, String lastFmUsername, int offset,
			int limit, String query) {
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getMostPlayedAlbums(spotifyEnabled, lastFmUsername, offset,
					limit, query));
		}
		return albums;
	}

	@Override
	public List<Album> getRandomAlbums(boolean spotifyEnabled, int limit) {
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getRandomAlbums(spotifyEnabled, limit));
		}
		return albums;
	}

	@Override
	public List<Album> getStarredAlbums(boolean spotifyEnabled, String lastFmUsername, int offset,
			int limit, String query) {
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getStarredAlbums(spotifyEnabled, lastFmUsername, offset, limit,
					query));
		}
		return albums;
	}
	
	@Override
	public List<Album> getAlbumsByArtist(boolean spotifyEnabled, int offset,
			int limit, String query) {
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getAlbumsByArtist(spotifyEnabled, offset, limit,
					query));
		}
		return albums;
	}
	
	@Override
	public List<Album> getAlbumsByGenre(boolean spotifyEnabled, int offset,
			int limit, String query, String genre) {
		
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getAlbumsByGenre(spotifyEnabled, offset, limit,
					query, genre));
		}
		return albums;
	}
	
	@Override
	public List<Album> getAlbumsByName(boolean spotifyEnabled, int offset,
			int limit, String query) {
		
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getAlbumsByName(spotifyEnabled, offset, limit,
					query));
		}
		return albums;
	}
	
	@Override
	public List<Album> getAlbumsByYear(boolean spotifyEnabled, int offset,
			int limit, String query, int fromYear, int toYear) {
		List<Album> albums = new ArrayList<Album>();
		for (LibraryBrowserDao dao : daos) {
			albums.addAll(dao.getAlbumsByYear(spotifyEnabled, offset, limit,
					query, fromYear, toYear));
		}
		return albums;
	}

	@Override
	public Track getTrack(Uri trackUri) {
		for (LibraryBrowserDao dao : daos) {
			Track track = dao.getTrack(trackUri);
			if (track != null) {
				return track;
			}
		}
		return null;
	}

	@Override
	public List<Track> getTracks(List<? extends Uri> trackUris) {
		List<Track> tracks = new ArrayList<Track>();
		for (LibraryBrowserDao dao : daos) {
			tracks.addAll(dao.getTracks(trackUris));
		}
		return tracks;
	}

	@Override
	public List<? extends Uri> getRecentlyPlayedTrackUris(
			String lastFmUsername, int offset, int limit, String query) {
		List<Uri> tracks = new ArrayList<Uri>();
		for (LibraryBrowserDao dao : daos) {
			tracks.addAll(dao.getRecentlyPlayedTrackUris(lastFmUsername,
					offset, limit, query));
		}
		return tracks;
	}

	@Override
	public List<? extends Uri> getMostPlayedTrackUris(String lastFmUsername,
			int offset, int limit, String query) {
		List<Uri> tracks = new ArrayList<Uri>();
		for (LibraryBrowserDao dao : daos) {
			tracks.addAll(dao.getMostPlayedTrackUris(lastFmUsername, offset,
					limit, query));
		}
		return tracks;

	}

	@Override
	public List<? extends Uri> getStarredTrackUris(String lastFmUsername,
			int offset, int limit, String query) {
		List<Uri> tracks = new ArrayList<Uri>();
		for (LibraryBrowserDao dao : daos) {
			tracks.addAll(dao.getStarredTrackUris(lastFmUsername, offset,
					limit, query));
		}
		return tracks;
	}

	@Override
	public List<? extends Uri> getRandomTrackUris(int limit) {
		List<Uri> tracks = new ArrayList<Uri>();
		for (LibraryBrowserDao dao : daos) {
			tracks.addAll(dao.getRandomTrackUris(limit));
		}
		return tracks;
	}

	@Override
	public List<? extends Uri> getRandomTrackUris(int limit, Integer fromYear,
			Integer toYear, String genre) {
		List<Uri> tracks = new ArrayList<Uri>();
		for (LibraryBrowserDao dao : daos) {
			tracks.addAll(dao
					.getRandomTrackUris(limit, fromYear, toYear, genre));
		}
		return tracks;
	}

	@Override
	public String getCoverArtFileForTrack(Uri uri) {
		for (LibraryBrowserDao dao : daos) {
			String cover = dao.getCoverArtFileForTrack(uri);
			if (cover != null) {
				return cover;
			}
		}
		return null;
	}

	@Override
	public void addArtwork(List<Track> tracks) {
		for (LibraryBrowserDao dao : daos) {
			dao.addArtwork(tracks);
		}

	}

	@Override
	public String getLyricsForTrack(Uri uri) {
		for (LibraryBrowserDao dao : daos) {
			String lyrics = dao.getLyricsForTrack(uri);
			if (lyrics != null) {
				return lyrics;
			}
		}
		return null;
	}

	@Override
	public String getLyricsForTrack(String artistName, String trackName) {
		for (LibraryBrowserDao dao : daos) {
			String lyrics = dao.getLyricsForTrack(artistName, trackName);
			if (lyrics != null) {
				return lyrics;
			}
		}
		return null;
	}

	@Override
	public List<Integer> getArtistIndexes() {
		List<Integer> indexes = new ArrayList<>();
		for (LibraryBrowserDao dao : daos) {
			indexes.addAll(dao.getArtistIndexes());
		}
		return indexes;
	}

	@Override
	public LibraryStatistics getStatistics() {
		for (LibraryBrowserDao dao : daos) {
			LibraryStatistics stats = dao.getStatistics();
			if (stats != null) {
				return stats;
			}
		}
		return null;

	}

	@Override
	public Uri getTrackUri(String filename) {
		for (LibraryBrowserDao dao : daos) {
			Uri uri = dao.getTrackUri(filename);
			if (uri != null) {
				return uri;
			}
		}
		return null;
	}

	@Override
	public Uri getTrackUri(File file) {
		for (LibraryBrowserDao dao : daos) {
			Uri uri = dao.getTrackUri(file);
			if (uri != null) {
				return uri;
			}
		}
		return null;
	}

	@Override
	public void markAllFilesForFullRescan() {
		for (LibraryBrowserDao dao : daos) {
			dao.markAllFilesForFullRescan();
		}
	}

	@Override
	public List<String> getFilesMissingMetadata() {
		List<String> files = new ArrayList<String>();
		for (LibraryBrowserDao dao : daos) {
			files.addAll(dao.getFilesMissingMetadata());
		}
		return files;
	}
	
	@Override
	public List<Track> getTracksByGenre(String genre, int offset, int limit) {
		List<Track> tracks = new ArrayList<Track>();
		for (LibraryBrowserDao dao : daos) {
			tracks.addAll(dao.getTracksByGenre(genre, offset, limit));
		}
		return tracks;
	}
}