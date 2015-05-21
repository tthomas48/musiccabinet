package com.github.hakko.musiccabinet.service.spotify;

import jahspotify.Query;
import jahspotify.Search;
import jahspotify.SearchListener;
import jahspotify.SearchResult;
import jahspotify.media.Link;
import jahspotify.query.AlbumQuery;
import jahspotify.query.TrackQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.hakko.musiccabinet.configuration.SpotifyUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.SearchCriteria;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.INameSearchService;
import com.github.hakko.musiccabinet.util.BlockingRequest;

public class SpotifySearchService implements INameSearchService {

	private static final Logger LOG = Logger.getLogger(SpotifyService.class);

	private SpotifyService spotifyService;

	@Override
	public NameSearchResult<Artist> getArtists(final String userQuery,
			int offset, int limit) {
		LOG.debug("In spotify artist search");

		if (!spotifyService.isSpotifyAvailable()
				|| !spotifyService.isLoggedIn()) {
			return new NameSearchResult<Artist>(Collections.EMPTY_LIST, offset);
		}

		SearchResult searchResult = spotifyService.search(Query
				.artist(userQuery));
		if (searchResult == null) {
			return new NameSearchResult<Artist>(Collections.EMPTY_LIST, offset);
		}

		List<Artist> result = new ArrayList<Artist>();
		List<Link> artists = searchResult.getArtistsFound();
		LOG.debug("Got back " + artists.size());
		for (Link link : artists) {
			jahspotify.media.Artist artist = spotifyService.readArtist(link);
			result.add(new Artist(link.asString(), artist.getName()));
		}
		LOG.debug(result);
		return new NameSearchResult(result, offset);
	}

	@Override
	public NameSearchResult<Album> getAlbums(final String userQuery,
			int offset, int limit) {
		if (!spotifyService.isSpotifyAvailable()
				|| !spotifyService.isLoggedIn()) {
			return new NameSearchResult<Album>(Collections.EMPTY_LIST, offset);
		}

		SearchResult searchResult = spotifyService.search(new AlbumQuery(
				userQuery));
		if (searchResult == null) {
			return new NameSearchResult<Album>(Collections.EMPTY_LIST, offset);
		}

		List<Album> result = new ArrayList<Album>();
		List<Link> albums = searchResult.getAlbumsFound();
		LOG.debug("Got back " + albums.size());
		for (Link link : albums) {
			jahspotify.media.Album album = spotifyService.readAlbum(link);
			jahspotify.media.Artist artist = spotifyService.readArtist(album
					.getArtist());
			result.add(new Album(new Artist(artist.getId().toString(), artist
					.getName()), new SpotifyUri(link), album.getName()));
		}
		LOG.debug(result);
		return new NameSearchResult(result, offset);
	}

	@Override
	public NameSearchResult<Track> getTracks(final String userQuery,
			int offset, int limit) {

		if (!spotifyService.isSpotifyAvailable()
				|| !spotifyService.isLoggedIn()) {
			return new NameSearchResult<Track>(Collections.EMPTY_LIST, offset);
		}

		SearchResult searchResult = spotifyService.search(new TrackQuery(
				userQuery));
		if (searchResult == null) {
			return new NameSearchResult<Track>(Collections.EMPTY_LIST, offset);
		}
		List<Track> result = new ArrayList<Track>();

		List<Link> tracks = searchResult.getTracksFound();
		LOG.debug("Got back " + tracks.size());
		for (Link link : tracks) {
			jahspotify.media.Track track = spotifyService.readTrack(link);
			jahspotify.media.Album album = spotifyService.readAlbum(track
					.getAlbum());
			jahspotify.media.Artist artist = spotifyService.readArtist(album
					.getArtist());

			MetaData md = new MetaData();
			md.setAlbum(album.getName());
			md.setAlbumUri(new SpotifyUri(track.getAlbum()));
			md.setArtist(artist.getName());
			md.setArtistUri(new SpotifyUri(album.getArtist()));
			md.setExplicit(track.isExplicit() ? 1 : 0);
			result.add(new Track(new SpotifyUri(link), track.getTitle(), md));
		}
		LOG.debug(result);
		return new NameSearchResult(result, offset);
	}

	@Override
	public List<? extends Uri> getTracks(SearchCriteria searchCriteria,
			int offset, int limit) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<String> getFileTypes() {
		return Collections.EMPTY_LIST;
	}

	public void setSpotifyService(SpotifyService spotifyService) {
		this.spotifyService = spotifyService;
	}

}
