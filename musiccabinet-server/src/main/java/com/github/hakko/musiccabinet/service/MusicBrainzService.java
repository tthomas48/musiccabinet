package com.github.hakko.musiccabinet.service;

import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.MusicBrainzAlbumDao;
import com.github.hakko.musiccabinet.dao.MusicBrainzArtistDao;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;
import com.github.hakko.musiccabinet.domain.model.music.MBArtist;
import com.github.hakko.musiccabinet.domain.model.music.MBRelease;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.musicbrainz.ArtistQueryParser;
import com.github.hakko.musiccabinet.parser.musicbrainz.ArtistQueryParserImpl;
import com.github.hakko.musiccabinet.parser.musicbrainz.ReleaseParser;
import com.github.hakko.musiccabinet.parser.musicbrainz.ReleaseParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.musicbrainz.ArtistQueryClient;
import com.github.hakko.musiccabinet.ws.musicbrainz.ReleaseClient;

public class MusicBrainzService {

	protected MusicBrainzArtistDao artistDao;
	protected MusicBrainzAlbumDao albumDao;

	protected ArtistQueryClient artistQueryClient;
	protected ReleaseClient releaseClient;
	
	protected boolean isIndexBeingCreated = false;
	protected int mbid, mbids, discography, discographies;
	
	private static final Logger LOG = Logger.getLogger(MusicBrainzService.class);
	
	public static final int TYPE_SINGLE = 0, TYPE_EP = 1, TYPE_ALBUM = 2;
	
	public boolean isIndexBeingCreated() {
		return isIndexBeingCreated;
	}
	
	public String getProgressDescription() {
		return String.format("%d/%d artist ids, %d/%d artist discographies", 
				mbid, mbids, discography, discographies);
	}
	
	public int getMissingAndOutdatedArtistsCount() {
		return artistDao.getMissingAndOutdatedArtistsCount();
	}
	
	public boolean hasDiscography() {
		return albumDao.hasDiscography();
	}
	
	public List<MBAlbum> getMissingAlbums(String artistName, List<Integer> albumTypes,
			String lastFmUsername, int playedWithinLastDays, int offset) {
		return albumDao.getMissingAlbums(artistName, albumTypes,
				lastFmUsername, playedWithinLastDays, offset);
	}
	
	public List<Album> getDiscography(Uri artistUri, boolean sortByYear, boolean sortAscending) {
		return albumDao.getDiscography(artistUri, sortByYear, sortAscending);
	}

	public void updateDiscography() {
		try {
			isIndexBeingCreated = true;
			mbid = mbids = discography = discographies = 0;
			updateArtistIds();
			updateArtistDiscographies();
			isIndexBeingCreated = false;
		} catch (Throwable t) {
			LOG.warn("MusicBrainz import failed unexpectedly!", t);
		}
	}
	
	protected void updateArtistIds() {
		List<Artist> missingArtists = artistDao.getMissingArtists();
		List<MBArtist> mbArtists = new ArrayList<>();
		mbids = missingArtists.size();
		for (Artist artist : artistDao.getMissingArtists()) {
			try {
				StringUtil response = new StringUtil(artistQueryClient.get(artist.getName()));
				ArtistQueryParser parser = new ArtistQueryParserImpl(response.getInputStream());
				if (parser.getArtist() != null) {
					mbArtists.add(parser.getArtist());
					if (mbArtists.size() > 100) {
						artistDao.createArtists(mbArtists);
						mbArtists.clear();
					}
				}
				++mbid;
			} catch (ApplicationException e) {
				LOG.warn("Couldn't read mbid for " + artist.getName(), e);
			}
		}
		artistDao.createArtists(mbArtists);
	}
	
	protected void updateArtistDiscographies() {
		List<MBArtist> outdatedArtists = artistDao.getOutdatedArtists();
		List<MBRelease> mbReleases = new ArrayList<>();
		ReleaseParser parser;
		discographies = outdatedArtists.size();
		for (MBArtist artist : outdatedArtists) {
			try {
				int offset = 0;
				do { 
					StringUtil response = new StringUtil(releaseClient.get(
						artist.getName(), artist.getMbid(), offset));
					parser = new ReleaseParserImpl(response.getInputStream());
					for (MBRelease album : parser.getReleases()) {
						album.setArtistUri(artist.getUri());
					}
					mbReleases.addAll(parser.getReleases());
					offset += 100;
				} while (offset < parser.getTotalReleases());
				++discography;
				if (mbReleases.size() > 1000) {
					albumDao.createAlbums(mbReleases);
					mbReleases.clear();
				}
			} catch (ApplicationException e) {
				LOG.warn("Couldn't read discography for " + artist.getName(), e);
			}
		}
		albumDao.createAlbums(mbReleases);
	}
	
	// Spring setters
	
	public void setMusicBrainzArtistDao(MusicBrainzArtistDao artistDao) {
		this.artistDao = artistDao;
	}

	public void setMusicBrainzAlbumDao(MusicBrainzAlbumDao albumDao) {
		this.albumDao = albumDao;
	}

	public void setArtistQueryClient(ArtistQueryClient artistQueryClient) {
		this.artistQueryClient = artistQueryClient;
	}

	public void setReleaseClient(ReleaseClient releaseClient) {
		this.releaseClient = releaseClient;
	}
	
}