package com.github.hakko.musiccabinet.service.spotify;

import jahspotify.media.Artist;
import jahspotify.media.Link;
import jahspotify.media.Link.Type;
import jahspotify.media.Playlist;
import jahspotify.services.MediaHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.github.hakko.musiccabinet.dao.AlbumInfoDao;
import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryBrowserDao;
import com.github.hakko.musiccabinet.dao.spotify.SpotifyLibraryBrowserDao;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.AlbumInfo;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.lastfm.SearchIndexUpdateService;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class MissingArtistService extends SearchIndexUpdateService {

	private static final int BATCH_SIZE = 1000;

	private static final Logger LOG = Logger
			.getLogger(MissingArtistService.class);

	private SpotifyService spotifyService;
	private SpotifyLibraryBrowserDao spotifyLibraryBrowserDao;
	private JdbcLibraryBrowserDao jdbcLibraryBrowserDao;
	private LibraryAdditionDao libraryAdditionDao;
	private AlbumInfoDao albumInfoDao;

	protected WebserviceHistoryService webserviceHistoryService;

	@Override
	public String getUpdateDescription() {
		return "missing spotify artists";
	}

	@Override
	protected void updateSearchIndex() throws ApplicationException {

		List<com.github.hakko.musiccabinet.domain.model.music.Artist> existingArtists = jdbcLibraryBrowserDao
				.getArtists();

		try {
			if (!spotifyService.lock()) {
				LOG.error("Unable to lock spotify");
			}
			List<Link> artists = new ArrayList<Link>();

			List<String> spotifyUsers = spotifyService
					.getSpotifySettingsService().getSpotifyUsers();

			for (String username : spotifyUsers) {
				Playlist starred = spotifyService.getSpotify().readStarredPlaylist(username, 0,0);
				if (!MediaHelper.waitFor(starred, 60)) {
					return;
				}

				for (Link trackLink : starred.getTracks()) {
					jahspotify.media.Track track = spotifyService.getSpotify()
							.readTrack(trackLink);
					if (!MediaHelper.waitFor(track, 60)) {
						continue;
					}
					for (Link artistLink : track.getArtists()) {
						if (!artists.contains(artistLink)) {
							artists.add(artistLink);
						}
					}
				}
			}

			Set<File> files = new HashSet<File>();
			List<Album> albums = new ArrayList<Album>();
			List<AlbumInfo> albumInfos = new ArrayList<AlbumInfo>();
			setTotalOperations(artists.size());
			ARTISTLINK: for (Link artistLink : artists) {
				albums.clear();

				Artist artist = spotifyService.getSpotify().readArtist(
						artistLink);
				if (!MediaHelper.waitFor(artist, 60)) {
					addFinishedOperation();
					continue ARTISTLINK;
				}

				for (com.github.hakko.musiccabinet.domain.model.music.Artist existingArtist : existingArtists) {
					if (existingArtist.getName().toLowerCase()
							.equals(artist.getName().toLowerCase())) {
						addFinishedOperation();
						continue ARTISTLINK;
					}
				}

				spotifyLibraryBrowserDao.getAlbums(albums, artist.getName(),
						false);

				NEXTALBUM: for (Album album : albums) {

					if (!album.getArtist().getName().toLowerCase()
							.equals(artist.getName().toLowerCase())) {
						continue NEXTALBUM;
					}

					// we don't want tons of singles
					if (album.getTrackUris().size() < 5) {
						continue NEXTALBUM;
					}
					
					AlbumInfo albumInfo = new AlbumInfo();
					albumInfo.setAlbum(album);
					albumInfo.setLargeImageUrl(album.getCoverArtURL());
					albumInfos.add(albumInfo);

					List<Track> tracks = spotifyLibraryBrowserDao
							.getTracks(album.getTrackUris());

					LOG.debug("Adding album " + album.getName() + ":"
							+ album.getArtist().getName());

					for (Track track : tracks) {
						File file = new File("spotify:", track.getUri()
								.toString(), new DateTime(), 0);
						file.setMetaData(track.getMetaData());
						files.add(file);
					}
				}

				if (files.size() > BATCH_SIZE) {
					libraryAdditionDao.addFiles("spotify:", files);
					libraryAdditionDao.updateLibrary();
					files.clear();
					
					albumInfoDao.createAlbumInfo(albumInfos);
					albumInfos.clear();
				}

				addFinishedOperation();

			}

			if (files.size() > 0) {
				libraryAdditionDao.addFiles("spotify:", files);
				libraryAdditionDao.updateLibrary();
				
				albumInfoDao.createAlbumInfo(albumInfos);
			}
		} finally {
			spotifyService.unlock();
		}
	}

	public void setSpotifyService(SpotifyService spotifyService) {
		this.spotifyService = spotifyService;
	}

	public void setSpotifyLibraryBrowserDao(
			SpotifyLibraryBrowserDao spotifyLibraryBrowserDao) {
		this.spotifyLibraryBrowserDao = spotifyLibraryBrowserDao;
	}

	public void setWebserviceHistoryService(
			WebserviceHistoryService webserviceHistoryService) {
		this.webserviceHistoryService = webserviceHistoryService;
	}

	public void setJdbcLibraryBrowserDao(
			JdbcLibraryBrowserDao jdbcLibraryBrowserDao) {
		this.jdbcLibraryBrowserDao = jdbcLibraryBrowserDao;
	}

	public void setLibraryAdditionDao(LibraryAdditionDao libraryAdditionDao) {
		this.libraryAdditionDao = libraryAdditionDao;
	}
	
	public void setAlbumInfoDao(AlbumInfoDao albumInfoDao) {
		this.albumInfoDao = albumInfoDao;
	}

}
