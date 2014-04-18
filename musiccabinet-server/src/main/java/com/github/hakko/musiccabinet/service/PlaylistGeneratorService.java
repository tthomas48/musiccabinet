package com.github.hakko.musiccabinet.service;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.PlaylistGeneratorDao;
import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;

/*
 * Exposes methods to generate playlists of relevant tracks, based on either an artist,
 * a single track, or one or more genres.
 */
public class PlaylistGeneratorService {

	protected PlaylistGeneratorDao dao;

	private static final Logger LOG = Logger.getLogger(PlaylistGeneratorService.class);
	
	/*
	 * Just make sure previous log file gets erased.
	 */
	public PlaylistGeneratorService() {
		LOG.info("MusicCabinet playlist service started at " + new Date() + ".");
	}

	public void setAllowedTrackLengthInterval(int minLength, int maxLength) {
		LOG.debug(format("allowed radio song length interval (%d, %d)", minLength, maxLength));
		dao.setAllowedTrackLengthInterval(minLength, maxLength);
	}
	
	/*
	 * TODO
	 * 
	 * We currently use a pre-calculated table (library.artisttoptrackplaycount)
	 * when searching for relevant artist tracks.
	 * 
	 * This should really be changed to using a materialized view, when time/
	 * Postgresql allows for it.
	 * 
	 * Until then, clear the table before removing data it refers to, and update
	 * it manually when new data has been added.
	 */
	public void updateSearchIndex() {
		long millis = -System.currentTimeMillis();
		dao.updateSearchIndex();
		millis += System.currentTimeMillis();
		LOG.info("Creating materialized view took " + (millis / 1000) + " sec.");
	}

	public boolean isSearchIndexCreated() {
		return dao.isSearchIndexCreated();
	}
	
	public List<? extends Uri> getTopTracksForArtist(Uri artistURI, int totalCount) throws ApplicationException {
		return dao.getTopTracksForArtist(artistURI, totalCount);
	}
	
	public List<? extends Uri> getPlaylistForArtist(Uri artistURI, int artistCount, int totalCount) throws ApplicationException {
		List<PlaylistItem> result = dao.getPlaylistForArtist(artistURI, artistCount, totalCount);
		Collections.shuffle(result);
		return distributeArtists(result);
	}
	
	public List<? extends Uri> getPlaylistForTags(String[] tags, int artistCount, int totalCount) {
		List<PlaylistItem> result = dao.getPlaylistForTags(tags, artistCount, totalCount);
		Collections.shuffle(result);
		return distributeArtists(result);
	}

	public List<? extends Uri> getPlaylistForGroup(String lastFmGroup, int artistCount, int totalCount) {
		List<PlaylistItem> result = dao.getPlaylistForGroup(lastFmGroup, artistCount, totalCount);
		Collections.shuffle(result);
		return distributeArtists(result);
	}

	public List<? extends Uri> getPlaylistForRelatedArtists(Uri artistURI, int artistCount, int totalCount) {
		return dao.getPlaylistForRelatedArtists(artistURI, artistCount, totalCount);
	}
	
	
	/*
	 * Approach: iterate over list of playlist items.
	 * If we find an element that is preceded with an element with the same artist (A),
	 * try finding first position to bubble it to by iterating forward, looking for
	 * the first occurrence of three items in a row with an artist different than (A).
	 * Then swap item (A) with the middle one in the group of three.
	 * 
	 * Using this with small lists (like AAABBB) won't work, it could end up as ABABBA.
	 * This is intentional, always having ABABAB isn't ideal.
	 */
	protected List<Uri> distributeArtists(List<PlaylistItem> list) {
		int size = list.size();
		for (int i = 1; i < size; i++) {
			int artistId = list.get(i).getArtistId();
			if (list.get(i - 1).getArtistId() == artistId) {
				for (int j = i + 5; j < i + 5 + size; j++) {
					int prev = (j - 1 + size) % size;
					int pos = j % size;
					int next = (j + 1) % size;
					if (list.get(prev).getArtistId() != artistId &&
						list.get(pos).getArtistId() != artistId &&
						list.get(next).getArtistId() != artistId) {
						Collections.swap(list, i, pos);
						break;
					}
				}
			}
		}
		List<Uri> trackIds = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			trackIds.add(new SubsonicUri(list.get(i).getTrackId()));
		}
		return trackIds;
	}

	// Spring setters
	
	public void setPlaylistGeneratorDao(PlaylistGeneratorDao playlistGeneratorDao) {
		this.dao = playlistGeneratorDao;
	}
	
}