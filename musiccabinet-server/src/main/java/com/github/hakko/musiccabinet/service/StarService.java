package com.github.hakko.musiccabinet.service;

import static java.lang.Integer.MAX_VALUE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.StarDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.LastFmSettingsService;
import com.github.hakko.musiccabinet.ws.lastfm.TrackLoveClient;
import com.github.hakko.musiccabinet.ws.lastfm.TrackUnLoveClient;

public class StarService {

	private Map<Integer, Set<Uri>> starredArtists = new HashMap<Integer, Set<Uri>>();
	private Map<Integer, Set<Uri>> starredAlbums = new HashMap<Integer, Set<Uri>>();
	private Map<Integer, Set<Uri>> starredTracks = new HashMap<Integer, Set<Uri>>();

	private Map<String, LastFmUser> cachedUsers = new HashMap<>();
	
	private StarDao starDao;
	private LastFmDao lastFmDao;
	private MusicDao musicDao;
	private LibraryBrowserDao browserDao;
	private LastFmSettingsService lastFmSettingsService;
	private TrackLoveClient trackLoveClient;
	private TrackUnLoveClient trackUnLoveClient;
	
	public void starArtist(String lastFmUsername, Uri artistUri) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.starArtist(lastFmUser, artistUri);
		getStarredArtistUris(lastFmUser).add(artistUri);
	}
	
	public void unstarArtist(String lastFmUsername, Uri artistUri) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.unstarArtist(lastFmUser, artistUri);
		getStarredArtistUris(lastFmUser).remove(artistUri);
	}
	
	protected Set<Uri> getStarredArtistUris(LastFmUser lastFmUser) {
		if (!starredArtists.containsKey(lastFmUser.getId())) {
			starredArtists.put(lastFmUser.getId(), new HashSet<>(
					starDao.getStarredArtistUris(lastFmUser, 0, MAX_VALUE, null)));
		}
		return starredArtists.get(lastFmUser.getId());
	}

	public List<Artist> getStarredArtists(String lastFmUsername) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		return musicDao.getArtists(getStarredArtistUris(lastFmUser));
	}
	
	public void starAlbum(String lastFmUsername, Uri albumUri) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.starAlbum(lastFmUser, albumUri);
		getStarredAlbumUris(lastFmUser).add(albumUri);
	}
	
	public void unstarAlbum(String lastFmUsername, Uri albumUri) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.unstarAlbum(lastFmUser, albumUri);
		getStarredAlbumUris(lastFmUser).remove(albumUri);
	}
	
	protected Set<Uri> getStarredAlbumUris(LastFmUser lastFmUser) {
		if (!starredAlbums.containsKey(lastFmUser.getId())) {
			starredAlbums.put(lastFmUser.getId(), new HashSet<>(
					starDao.getStarredAlbumUris(lastFmUser, 0, MAX_VALUE, null)));
		}
		return starredAlbums.get(lastFmUser.getId());
	}

	public void starTrack(String lastFmUsername, Uri trackUri) throws ApplicationException {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.starTrack(lastFmUser, trackUri);
		getStarredTracks(lastFmUser).add(trackUri);
		if (lastFmSettingsService.isSyncStarredAndLovedTracks()) {
			trackLoveClient.love(browserDao.getTrack(trackUri), lastFmUser);
		}
	}
	
	public void unstarTrack(String lastFmUsername, Uri trackUri) throws ApplicationException {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.unstarTrack(lastFmUser, trackUri);
		getStarredTracks(lastFmUser).remove(trackUri);
		if (lastFmSettingsService.isSyncStarredAndLovedTracks()) {
			trackUnLoveClient.unlove(browserDao.getTrack(trackUri), lastFmUser);
		}
	}
	
	protected Set<Uri> getStarredTracks(LastFmUser lastFmUser) {
		if (!starredTracks.containsKey(lastFmUser.getId())) {
			starredTracks.put(lastFmUser.getId(), new HashSet<>(
					starDao.getStarredTrackUris(lastFmUser, 0, MAX_VALUE, null)));
		}
		return starredTracks.get(lastFmUser.getId());
	}

	protected LastFmUser getLastFmUser(String lastFmUsername) {
		if (cachedUsers.containsKey(lastFmUsername)) {
			return cachedUsers.get(lastFmUsername);
		}
		LastFmUser lastFmUser = lastFmDao.getLastFmUser(lastFmUsername);
		cachedUsers.put(lastFmUsername, lastFmUser);
		return lastFmUser;
	}

	public boolean isArtistStarred(String lastFmUsername, Uri artistUri) {
		if (lastFmUsername == null) {
			return false;
		}
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		return getStarredArtistUris(lastFmUser).contains(artistUri);
	}

	public boolean[] getStarredAlbumsMask(String lastFmUsername, List<? extends Uri> albumIds) {
		boolean[] mask = new boolean[albumIds.size()];
		if (lastFmUsername == null) {
			return mask;
		}
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		for (int i = 0; i < mask.length; i++) {
			mask[i] = getStarredAlbumUris(lastFmUser).contains(albumIds.get(i));
		}
		return mask;
	}

	public boolean[] getStarredTracksMask(String lastFmUsername, List<? extends Uri> trackUris) {
		boolean[] mask = new boolean[trackUris.size()];
		if (lastFmUsername == null) {
			return mask;
		}
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		for (int i = 0; i < mask.length; i++) {
			mask[i] = getStarredTracks(lastFmUser).contains(trackUris.get(i));
		}
		return mask;
	}

	public void clearTrackCache() {
		starredTracks.clear();
	}

	// Spring setters
	
	public void setStarDao(StarDao starDao) {
		this.starDao = starDao;
	}

	public void setLastFmDao(LastFmDao lastFmDao) {
		this.lastFmDao = lastFmDao;
	}

	public void setMusicDao(MusicDao musicDao) {
		this.musicDao = musicDao;
	}

	public void setLibraryBrowserDao(LibraryBrowserDao browserDao) {
		this.browserDao = browserDao;
	}

	public void setLastFmSettingsService(LastFmSettingsService lastFmSettingsService) {
		this.lastFmSettingsService = lastFmSettingsService;
	}

	public void setTrackLoveClient(TrackLoveClient trackLoveClient) {
		this.trackLoveClient = trackLoveClient;
	}

	public void setTrackUnLoveClient(TrackUnLoveClient trackUnLoveClient) {
		this.trackUnLoveClient = trackUnLoveClient;
	}

}