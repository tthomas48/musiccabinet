package com.github.hakko.musiccabinet.service;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.SearchCriteria;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface INameSearchService {
	public NameSearchResult<Artist> getArtists(String userQuery, int offset,
			int limit);

	public NameSearchResult<Album> getAlbums(String userQuery, int offset,
			int limit);

	public NameSearchResult<Track> getTracks(String userQuery, int offset,
			int limit);

	public List<? extends Uri> getTracks(SearchCriteria searchCriteria, int offset,
			int limit);

	public List<String> getFileTypes();
}
