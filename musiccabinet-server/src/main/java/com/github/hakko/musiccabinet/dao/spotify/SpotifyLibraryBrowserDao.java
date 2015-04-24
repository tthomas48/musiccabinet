package com.github.hakko.musiccabinet.dao.spotify;

import jahspotify.media.Link;
import jahspotify.services.MediaHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.hakko.musiccabinet.configuration.SpotifyUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.dao.util.URIUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.LibraryStatistics;
import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.INameSearchService;
import com.github.hakko.musiccabinet.service.spotify.SpotifyService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class SpotifyLibraryBrowserDao implements LibraryBrowserDao {

	private static final Logger LOG = Logger
			.getLogger(SpotifyLibraryBrowserDao.class);

	private SpotifyService spotifyService;
	private INameSearchService nameSearchService;

	private Cache<Uri, jahspotify.media.Album> albumCache = CacheBuilder
			.newBuilder().maximumSize(100)
			.expireAfterWrite(1, TimeUnit.MINUTES).build();

	private Cache<Uri, jahspotify.media.Artist> artistCache = CacheBuilder
			.newBuilder().maximumSize(100)
			.expireAfterWrite(1, TimeUnit.MINUTES).build();

	@Override
	public boolean hasArtists() {
		return false;
	}

	@Override
	public Artist getArtist(String artistName) {
		return null;
	}

	@Override
	public List<Artist> getArtists() {
		return new ArrayList<Artist>();
	}

	@Override
	public List<Artist> getArtists(int indexLetter) {
		return new ArrayList<Artist>();
	}

	@Override
	public List<Artist> getArtists(String tag, int treshold) {
		return new ArrayList<Artist>();
	}

	@Override
	public List<ArtistRecommendation> getRecentlyPlayedArtists(
			String lastFmUsername, boolean onlyAlbumArtists, int offset,
			int limit, String query) {
		return new ArrayList<ArtistRecommendation>();
	}

	@Override
	public List<ArtistRecommendation> getMostPlayedArtists(
			String lastFmUsername, int offset, int limit, String query) {
		return new ArrayList<ArtistRecommendation>();
	}

	@Override
	public List<ArtistRecommendation> getRandomArtists(
			boolean onlyAlbumArtists, int limit) {
		return new ArrayList<ArtistRecommendation>();
	}

	@Override
	public List<ArtistRecommendation> getStarredArtists(String lastFmUsername,
			int offset, int limit, String query) {
		return new ArrayList<ArtistRecommendation>();
	}

	@Override
	public Album getAlbum(Uri albumUri) {
		if (albumUri instanceof SpotifyUri == false) {
			return null;
		}
		try {
			if (!spotifyService.lock()) {
				return null;
			}

			LOG.debug("Trying to get album " + albumUri);
			jahspotify.media.Album spotifyAlbum = loadAlbum(albumUri);
			if (spotifyAlbum == null) {
				return null;
			}

			List<Link> trackLinks = spotifyAlbum.getTracks();
			List<Uri> tracks = new ArrayList<Uri>();
			for (Link track : trackLinks) {
				tracks.add(new SpotifyUri(track));
			}
			return new Album(new SpotifyUri(spotifyAlbum.getArtist()),
					spotifyAlbum.getArtist().getId(), albumUri,
					spotifyAlbum.getName(), spotifyAlbum.getYear(),
					spotifyAlbum.getCover().asString(), false, spotifyAlbum
							.getCover().asString(), tracks, new SpotifyUri(
							spotifyAlbum.getId()));
		} finally {
			spotifyService.unlock();
		}
	}

	private jahspotify.media.Album loadAlbum(Uri albumUri) {
		jahspotify.media.Album spotifyAlbum = albumCache.getIfPresent(albumUri);
		if (spotifyAlbum == null) {

			spotifyAlbum = spotifyService.getSpotify().readAlbum(
					((SpotifyUri) albumUri).getLink(), true);
			if (!MediaHelper.waitFor(spotifyAlbum, 60)) {
				return null;
			}
			albumCache.put(albumUri, spotifyAlbum);
		}
		return spotifyAlbum;
	}

	private jahspotify.media.Artist loadArtist(Uri artistUri) {
		jahspotify.media.Artist spotifyArtist = artistCache
				.getIfPresent(artistUri);
		if (spotifyArtist == null) {
			spotifyArtist = spotifyService.getSpotify().readArtist(
					((SpotifyUri) artistUri).getLink(), true);
			if (!MediaHelper.waitFor(spotifyArtist, 60)) {
				return null;
			}
			artistCache.put(artistUri, spotifyArtist);
		}
		return spotifyArtist;
	}

	@Override
	public void getAlbums(List<Album> albums, Artist artist,
			boolean sortAscending) {

		// we only want to come through this path if we're coming out
		// of search's
		if (!URIUtil.isSpotify(artist.getUri())) {
			return;
		}

		getAlbums(albums, artist.getName(), artist.getSpotifyUri(), sortAscending);
	}

	public void getAlbums(List<Album> albums, String artistName, Uri spotifyUri, 
			boolean sortAscending) {

		Artist foundArtist = null;
		NameSearchResult<Artist> artists = nameSearchService.getArtists(
				artistName, 0, 5);
		for (Artist a : artists.getResults()) {
			if (spotifyUri != null && spotifyUri.equals(a.getSpotifyUri())) {
				foundArtist = a;
				break;
			}
			if (spotifyUri != null && spotifyUri.equals(a.getUri())) {
				foundArtist = a;
				break;
			}
			// fall back to this
			if (a.getName().toLowerCase().equals(artistName.toLowerCase())) {
				foundArtist = a;
				break;
			}
		}
		if (foundArtist == null) {
			return;
		}
		Uri artistUri = foundArtist.getSpotifyUri() != null ? foundArtist.getSpotifyUri() : foundArtist.getUri();

		try {
			if (!spotifyService.lock()) {
				return;
			}

			jahspotify.media.Artist spotifyArtist = loadArtist(artistUri);

			List<Link> albumLinks = spotifyArtist.getAlbums();
			ALBUMS: for (Link albumLink : albumLinks) {
				jahspotify.media.Album spotifyAlbum = loadAlbum(new SpotifyUri(
						albumLink));
				if (spotifyAlbum == null) {
					continue ALBUMS;
				}

				jahspotify.media.Artist albumArtist = loadArtist(new SpotifyUri(
						spotifyAlbum.getArtist()));
				if (albumArtist == null) {
					continue ALBUMS;
				}

				for (Album album : albums) {
					if (spotifyAlbum.getName().toLowerCase()
							.equals(album.getName().toLowerCase())) {
						continue ALBUMS;
					}
				}

				List<Link> trackLinks = spotifyAlbum.getTracks();
				List<Uri> tracks = new ArrayList<Uri>();
				for (Link track : trackLinks) {
					tracks.add(new SpotifyUri(track));
				}
				// Image image =
				// spotifyService.getSpotify().readImage(spotifyAlbum.getCover());
				String albumName = spotifyAlbum.getName();
				Integer year = spotifyAlbum.getYear();
				Link cover = spotifyAlbum.getCover();
				Uri albumArtistUri = new SpotifyUri(albumArtist.getId());
				String albumArtistName = albumArtist.getName();
				Uri albumUri = new SpotifyUri(albumLink);
				String coverUri = "";
				if (cover != null) {
					coverUri = cover.asString();
				}
				Uri albumLinkUri = new SpotifyUri(albumLink);
				albums.add(new Album(albumArtistUri, albumArtistName, albumUri,
						albumName, year, coverUri, false, coverUri, tracks,
						albumLinkUri));
			}
		} finally {
			spotifyService.unlock();
		}
	}

	@Override
	public void getAlbums(List<Album> albums, Artist artist,
			boolean sortByYear, boolean sortAscending) {
		// TODO how do we do sorting?
		getAlbums(albums, artist, sortAscending);
	}

	@Override
	public List<Album> getVariousArtistsAlbums() {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getRecentlyAddedAlbums(boolean spotifyEnabled, int offset, int limit,
			String query) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getRecentlyPlayedAlbums(boolean spotifyEnabled, String lastFmUsername,
			int offset, int limit, String query) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getMostPlayedAlbums(boolean spotifyEnabled, String lastFmUsername, int offset,
			int limit, String query) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getRandomAlbums(boolean spotifyEnabled, int limit) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getStarredAlbums(boolean spotifyEnabled, String lastFmUsername, int offset,
			int limit, String query) {

		/*
		 * SearchResult searchResult = new BlockingRequest<SearchResult>() {
		 * 
		 * @Override public void run() {
		 * spotifyService.getSpotify().initiateSearch( new
		 * Search(Query.token("spotify:starred")), new SearchListener() {
		 * 
		 * @Override public void searchComplete(SearchResult searchResult) {
		 * finish(searchResult); } });
		 * 
		 * } }.start();
		 * 
		 * List<Album> result = new ArrayList<Album>(); List<Link> albums =
		 * searchResult.getAlbumsFound(); LOG.debug("Got back " +
		 * albums.size()); for (Link link : albums) { jahspotify.media.Album
		 * album = spotifyService.getSpotify().readAlbum(link, true);
		 * 
		 * List<Link> trackLinks = album.getTracks(); List<Uri> tracks = new
		 * ArrayList<Uri>(); for(Link track : trackLinks) { tracks.add(new
		 * SpotifyUri(track)); }
		 * 
		 * jahspotify.media.Artist artist =
		 * spotifyService.getSpotify().readArtist(album.getArtist(), true);
		 * Album albumModel = new Album(new Artist(artist.getId().toString(),
		 * artist.getName()), new SpotifyUri(link), album.getName());
		 * albumModel.setTrackUris(tracks); result.add(albumModel); }
		 * LOG.debug(result); //return new NameSearchResult(result, offset);
		 */

		/*
		 * List<Album> result = new ArrayList<Album>();
		 * if(spotifyService.isSpotifyAvailable()) { SearchResult searchResult =
		 * spotifyService.getSpotify().f if(!MediaHelper.waitFor(searchResult,
		 * 60)) { return result; } List<Link> albums =
		 * searchResult.getAlbumsFound(); for (Link link : albums) {
		 * jahspotify.media.Album album =
		 * spotifyService.getSpotify().readAlbum(link, true);
		 * 
		 * List<Link> trackLinks = album.getTracks(); List<Uri> tracks = new
		 * ArrayList<Uri>(); for(Link track : trackLinks) { tracks.add(new
		 * SpotifyUri(track)); }
		 * 
		 * jahspotify.media.Artist artist =
		 * spotifyService.getSpotify().readArtist(album.getArtist(), true);
		 * Album albumModel = new Album(new Artist(artist.getId().toString(),
		 * artist.getName()), new SpotifyUri(link), album.getName());
		 * albumModel.setTrackUris(tracks); result.add(albumModel); }
		 * LOG.debug(result); }
		 */
		// return result;
		return new ArrayList<Album>();
	}

	@Override
	public Track getTrack(Uri trackUri) {
		return null;
	}

	@Override
	public List<Track> getTracks(List<? extends Uri> trackUris) {
		List<Track> tracks = new ArrayList<Track>();
		try {
			if (!spotifyService.lock()) {
				return tracks;
			}

			for (Uri uri : trackUris) {
				if (URIUtil.isSpotify(uri)) {
					jahspotify.media.Track spotifyTrack = spotifyService
							.getSpotify().readTrack(
									((SpotifyUri) uri).getLink());
					if (!MediaHelper.waitFor(spotifyTrack, 10)) {
						continue;
					}
					MetaData md = new MetaData();

					jahspotify.media.Album album = loadAlbum(new SpotifyUri(
							spotifyTrack.getAlbum()));
					if(album == null) {
						continue;
					}
					md.setAlbum(album.getName());

					jahspotify.media.Artist artist = loadArtist(new SpotifyUri(
							album.getArtist()));
					if (!MediaHelper.waitFor(artist, 10)) {
						continue;
					}

					md.setUri(uri);
					md.setMediaType(MetaData.Mediatype.MP3);
					md.setArtist(artist.getName());
					md.setTrackNr((short) spotifyTrack.getTrackNumber());
					md.setDiscNr((short) 1);
					md.setDuration((short) (spotifyTrack.getLength() / 1000));
					md.setYear(album.getYear());
					md.setAlbumUri(new SpotifyUri(spotifyTrack.getAlbum()));
					md.setArtistUri(new SpotifyUri(spotifyTrack.getArtists()
							.get(0)));
					md.setModified(new Date().getTime());
					md.setBitrate((short) 0);
					md.setSize(0l);
					md.setPath(uri.toString());
					md.setTitle(spotifyTrack.getTitle());
					md.setExplicit(spotifyTrack.isExplicit() ? 1 : 0);
					tracks.add(new Track(uri, spotifyTrack.getTitle(), md));
				}
			}
			return tracks;
		} finally {
			spotifyService.unlock();
		}

	}

	@Override
	public List<? extends Uri> getRecentlyPlayedTrackUris(
			String lastFmUsername, int offset, int limit, String query) {
		return new ArrayList<Uri>();
	}

	@Override
	public List<? extends Uri> getMostPlayedTrackUris(String lastFmUsername,
			int offset, int limit, String query) {
		return new ArrayList<Uri>();
	}

	@Override
	public List<? extends Uri> getStarredTrackUris(String lastFmUsername,
			int offset, int limit, String query) {
		// this should be handled in the MissingArtistService
		return new ArrayList<>();
		/*
		try {
			if (!spotifyService.lock()) {
				return null;
			}
			jahspotify.media.Playlist playlist = spotifyService.getSpotify()
					.readPlaylist(
							Link.create("spotify:user:"
									+ spotifyService.getSpotifyUser()
											.getUserName() + ":starred"), 0,
							1000);
			if (!MediaHelper.waitFor(playlist, 60)) {
				return null;
			}
			List<Link> trackLinks = playlist.getTracks();
			List<Uri> uris = new ArrayList<Uri>();
			for (Link trackLink : trackLinks) {
				uris.add(new SpotifyUri(trackLink));
			}
			return uris;

		} finally {
			spotifyService.unlock();
		}
		*/
	}

	@Override
	public List<? extends Uri> getRandomTrackUris(int limit) {
		return new ArrayList<Uri>();
	}

	@Override
	public List<? extends Uri> getRandomTrackUris(int limit, Integer fromYear,
			Integer toYear, String genre) {
		return new ArrayList<Uri>();
	}

	@Override
	public String getCoverArtFileForTrack(Uri uri) {
		return null;
	}

	@Override
	public void addArtwork(List<Track> tracks) {
	}

	@Override
	public String getLyricsForTrack(Uri uri) {
		return null;
	}

	@Override
	public String getLyricsForTrack(String artistName, String trackName) {
		return null;
	}

	@Override
	public List<Integer> getArtistIndexes() {
		return new ArrayList<Integer>();
	}

	@Override
	public LibraryStatistics getStatistics() {
		return null;
	}

	@Override
	public Uri getTrackUri(String filename) {
		return URIUtil.parseURI(filename);
	}

	@Override
	public Uri getTrackUri(File file) {
		return null;
	}

	@Override
	public void markAllFilesForFullRescan() {

	}

	@Override
	public List<String> getFilesMissingMetadata() {
		return new ArrayList<String>();
	}

	public void setSpotifyService(SpotifyService spotifyService) {
		this.spotifyService = spotifyService;
	}

	public void setNameSearchService(INameSearchService nameSearchService) {
		this.nameSearchService = nameSearchService;
	}

	@Override
	public List<Album> getAlbumsByName(boolean spotifyEnabled, int offset, int limit, String query) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getAlbumsByArtist(boolean spotifyEnabled, int offset, int limit, String query) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getAlbumsByYear(boolean spotifyEnabled, int offset, int limit, String query,
			int fromYear, int toYear) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getAlbumsByGenre(boolean spotifyEnabled, int offset, int limit, String query,
			String genre) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Track> getTracksByGenre(String genre, int offset, int limit) {
		return new ArrayList<Track>();
	}

}
