package com.github.hakko.musiccabinet.service.spotify;

import jahspotify.Query;
import jahspotify.Search;
import jahspotify.SearchListener;
import jahspotify.SearchResult;
import jahspotify.media.Link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
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
				|| !spotifyService.getSpotify().isLoggedIn()) {
			return new NameSearchResult<Artist>(Collections.EMPTY_LIST, offset);
		}

		SearchResult searchResult = new BlockingRequest<SearchResult>() {
			@Override
			public void run() {
				spotifyService.getSpotify().initiateSearch(
						new Search(Query.artist(userQuery)),
						new SearchListener() {
							@Override
							public void searchComplete(SearchResult searchResult) {
								finish(searchResult);
							}
						});

			}
		}.start();

		List<Artist> result = new ArrayList<Artist>();
		List<Link> artists = searchResult.getArtistsFound();
		LOG.debug("Got back " + artists.size());
		for (Link link : artists) {
			jahspotify.media.Artist artist = spotifyService.getSpotify().readArtist(link, true);
			result.add(new Artist(link.asString(), artist.getName()));
		}
		LOG.debug(result);
		return new NameSearchResult(result, offset);
	}

	@Override
	public NameSearchResult<Album> getAlbums(String userQuery, int offset,
			int limit) {
		return new NameSearchResult<Album>(Collections.EMPTY_LIST, offset);
	}

	@Override
	public NameSearchResult<Track> getTracks(String userQuery, int offset,
			int limit) {
		return new NameSearchResult<Track>(Collections.EMPTY_LIST, offset);
	}

	@Override
	public List<Integer> getTracks(SearchCriteria searchCriteria, int offset,
			int limit) {
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
