package com.github.hakko.musiccabinet.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.SearchCriteria;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class CompositeNameSearchService implements INameSearchService {

	public Set<INameSearchService> services = new HashSet<INameSearchService>();

	public Set<INameSearchService> getServices() {
		return services;
	}

	public void setServices(Set<INameSearchService> services) {
		this.services = services;
	}

	@Override
	public NameSearchResult<Artist> getArtists(String userQuery, int offset,
			int limit) {

		Set<Artist> results = new HashSet<Artist>();
		for (INameSearchService service : services) {
			NameSearchResult<Artist> searchResult = service.getArtists(
					userQuery, offset, limit);
			for (Artist artist : searchResult.getResults()) {
				if (!results.contains(artist)) {
					results.add(artist);
				}
			}
		}
		return new NameSearchResult<Artist>(new ArrayList<Artist>(results),
				offset);
	}

	@Override
	public NameSearchResult<Album> getAlbums(String userQuery, int offset,
			int limit) {
		Set<Album> results = new HashSet<Album>();
		for (INameSearchService service : services) {
			NameSearchResult<Album> searchResult = service.getAlbums(userQuery,
					offset, limit);
			for (Album album : searchResult.getResults()) {
				if (!results.contains(album)) {
					results.add(album);
				}
			}
		}
		return new NameSearchResult<Album>(new ArrayList<Album>(results),
				offset);
	}

	@Override
	public NameSearchResult<Track> getTracks(String userQuery, int offset,
			int limit) {
		Set<Track> results = new HashSet<Track>();
		for (INameSearchService service : services) {
			NameSearchResult<Track> searchResult = service.getTracks(userQuery,
					offset, limit);
			for (Track track : searchResult.getResults()) {
				if (!results.contains(track)) {
					results.add(track);
				}
			}
		}
		return new NameSearchResult<Track>(new ArrayList<Track>(results),
				offset);

	}

	@Override
	public List<Uri> getTracks(SearchCriteria searchCriteria, int offset,
			int limit) {
		Set<Uri> results = new HashSet<Uri>();
		for (INameSearchService service : services) {
			List<? extends Uri> searchResult = service.getTracks(searchCriteria,
					offset, limit);
			for (Uri trackUri : searchResult) {
				if (!results.contains(trackUri)) {
					results.add(trackUri);
				}
			}
		}
		return new ArrayList<Uri>(results);
	}

	@Override
	public List<String> getFileTypes() {
		List<String> results = new ArrayList<String>();
		for (INameSearchService service : services) {
			for (String type : service.getFileTypes()) {
				if (!results.contains(type)) {
					results.add(type);
				}
			}
		}
		return results;

	}

	public void addServices(INameSearchService iSearchService) {
		this.services.add(iSearchService);
	}

}
