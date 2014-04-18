package com.github.hakko.musiccabinet.dao.spotify;

import jahspotify.media.Image;
import jahspotify.media.Link;
import jahspotify.services.MediaHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.hakko.musiccabinet.configuration.SpotifyUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.dao.util.URIUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.LibraryStatistics;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.spotify.SpotifyService;

public class SpotifyLibraryBrowserDao implements LibraryBrowserDao {
	
	private static final Logger LOG = Logger
			.getLogger(SpotifyLibraryBrowserDao.class);

	private SpotifyService spotifyService;


	@Override
	public boolean hasArtists() {
		return false;
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
		if(albumUri instanceof SpotifyUri == false) {
			return null;
		}
		LOG.debug("Trying to get album " + albumUri);
		jahspotify.media.Album spotifyAlbum = spotifyService.getSpotify().readAlbum(((SpotifyUri)albumUri).getLink(), true);
		if(!MediaHelper.waitFor(spotifyAlbum, 60)) {
			return null;
		}
		List<Link> trackLinks = spotifyAlbum.getTracks();
		List<Uri> tracks = new ArrayList<Uri>();
		for(Link track : trackLinks) {
			tracks.add(new SpotifyUri(track));
		}
		return new Album(new SpotifyUri(spotifyAlbum.getArtist().getUri()), spotifyAlbum.getArtist().getId(), 
				albumUri, spotifyAlbum.getName(), (short) spotifyAlbum.getYear(), 
				spotifyAlbum.getCover().asString(),
				false, 
				spotifyAlbum.getCover().asString(), tracks);
	}

	@Override
	public List<Album> getAlbums(Uri artistUri, boolean sortAscending) {
		if(artistUri instanceof SpotifyUri == false) {
			System.err.println("Not spotify URI");
				return new ArrayList<Album>();
		}
		System.err.println("Attempting to get Albums");
		List<Album> albums = new ArrayList<Album>();
		
		jahspotify.media.Artist spotifyArtist = spotifyService.getSpotify().readArtist(((SpotifyUri)artistUri).getLink(), true);
		if(!MediaHelper.waitFor(spotifyArtist, 60)) {
			return new ArrayList<Album>();
		}
		
		List<Link> albumLinks = spotifyArtist.getAlbums();
		System.err.println(albumLinks);
		for(Link albumLink : albumLinks) {
			jahspotify.media.Album spotifyAlbum = spotifyService.getSpotify().readAlbum(albumLink, true);
			if(!MediaHelper.waitFor(spotifyAlbum, 60)) {
				continue;
			}
			
			
			spotifyService.getSpotify().readArtist(spotifyAlbum.getArtist());
			List<Link> trackLinks = spotifyAlbum.getTracks();
			List<Uri> tracks = new ArrayList<Uri>();
			for(Link track : trackLinks) {
				tracks.add(new SpotifyUri(track));
			}
			Image image = spotifyService.getSpotify().readImage(spotifyAlbum.getCover());
			System.err.println(image.getId());
			albums.add(new Album(new SpotifyUri(spotifyArtist.getId()), spotifyArtist.getName(), 
					new SpotifyUri(albumLink), spotifyAlbum.getName(), (short) spotifyAlbum.getYear(), 
					spotifyAlbum.getCover().asString(),
					false, 
					spotifyAlbum.getCover().asString(), tracks));
			
		}
		
		return albums;
		
		
	}

	@Override
	public List<Album> getAlbums(Uri artistUri, boolean sortByYear,
			boolean sortAscending) {
		// TODO how do we do sorting?
		return getAlbums(artistUri, sortAscending);
	}

	@Override
	public List<Album> getVariousArtistsAlbums() {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getRecentlyAddedAlbums(int offset, int limit,
			String query) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getRecentlyPlayedAlbums(String lastFmUsername,
			int offset, int limit, String query) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getMostPlayedAlbums(String lastFmUsername, int offset,
			int limit, String query) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getRandomAlbums(int limit) {
		return new ArrayList<Album>();
	}

	@Override
	public List<Album> getStarredAlbums(String lastFmUsername, int offset,
			int limit, String query) {
		return new ArrayList<Album>();
	}

	@Override
	public Track getTrack(Uri trackUri) {
		return null;
	}

	@Override
	public List<Track> getTracks(List<? extends Uri> trackUris) {
		List<Track> tracks = new ArrayList<Track>();
		for(Uri uri : trackUris) {
			if(URIUtil.isSpotify(uri)) {
				jahspotify.media.Track spotifyTrack = spotifyService.getSpotify().readTrack(((SpotifyUri)uri).getLink());
				if(!MediaHelper.waitFor(spotifyTrack, 60)) {
					continue;
				}
				MetaData md = new MetaData();
				
				jahspotify.media.Album album = spotifyService.getSpotify().readAlbum(spotifyTrack.getAlbum(), true);
				//MediaHelper.waitFor(album, 60);
				md.setAlbum(album.getName());
				
				jahspotify.media.Artist artist = spotifyService.getSpotify().readArtist(album.getArtist(), true);
				//MediaHelper.waitFor(artist, 60);
				
				md.setMediaType(MetaData.Mediatype.MP3);
				md.setArtist(artist.getName());
				md.setTrackNr((short) spotifyTrack.getTrackNumber());
				md.setDiscNr((short)1);
				md.setDuration((short) spotifyTrack.getLength());
				md.setYear((short) album.getYear());
				md.setAlbumUri(new SpotifyUri(spotifyTrack.getAlbum()));
				md.setArtistUri(new SpotifyUri(spotifyTrack.getArtists().get(0)));
				md.setModified(new Date().getTime());
				md.setBitrate((short)0);
				md.setSize(0);
				md.setPath(uri.toString());
				tracks.add(new Track(uri, spotifyTrack.getTitle(), md));
			}
		}
		return tracks;
		
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
		return new ArrayList<Uri>();
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
		return null;
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
	

}
