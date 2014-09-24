package com.github.hakko.musiccabinet.service.spotify;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.SPOTIFY_MISSING_ALBUMS;

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
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.lastfm.SearchIndexUpdateService;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class MissingAlbumService extends SearchIndexUpdateService {

	private static final Logger LOG = Logger
			.getLogger(MissingAlbumService.class);

	private static final int BATCH_SIZE = 1000;

	private SpotifyService spotifyService;
	private SpotifyLibraryBrowserDao spotifyLibraryBrowserDao;
	private JdbcLibraryBrowserDao jdbcLibraryBrowserDao;
	private LibraryAdditionDao libraryAdditionDao;
	private AlbumInfoDao albumInfoDao;

	protected WebserviceHistoryService webserviceHistoryService;

	@Override
	public String getUpdateDescription() {
		return "missing spotify albums";
	}

	@Override
	protected void updateSearchIndex() throws ApplicationException {

		Set<String> artistNames = webserviceHistoryService
				.getArtistNamesScheduledForUpdate(SPOTIFY_MISSING_ALBUMS);

		setTotalOperations(artistNames.size());

		// add the spotify virtual directory if it doesn't exist
		Set<String> spotifyDir = new HashSet<String>();
		spotifyDir.add("spotify:");
		libraryAdditionDao.addSubdirectories(null, spotifyDir);

		Set<File> files = new HashSet<File>();
		List<Album> existingAlbums = new ArrayList<Album>();
		List<Album> albums = new ArrayList<Album>();
		List<AlbumInfo> albumInfos = new ArrayList<AlbumInfo>();
		for (String artistName : artistNames) {
			try {
				albums.clear();
				existingAlbums.clear();
				
				if(artistName.toLowerCase().contains("various")) {
					LOG.debug("Skipping " + artistName);
					addFinishedOperation();
					continue;
				}

				Artist artist = jdbcLibraryBrowserDao.getArtist(artistName);
				if (artist == null) {
					LOG.debug("Could not find artist " + artistName);
					addFinishedOperation();
					continue;
				}
				
				if (jdbcLibraryBrowserDao.isStarred(artist) == false) {
					continue;
				}
				
				jdbcLibraryBrowserDao.getAlbums(existingAlbums, artist, false);

				albums.addAll(existingAlbums);
				spotifyLibraryBrowserDao.getAlbums(albums, artistName, false);

				NEXTALBUM: for (Album album : albums) {
					// currently I don't want Various Artists junk and guest
					// starring stuff
					// just the albums man
					for (Album existingAlbum : existingAlbums) {
						// we want to get rid of "BLAH BLAH (Deluxe Version)
						if (spotifyService.cleanAlbumName(album
								.getName()
								)
								.startsWith(
										spotifyService.cleanAlbumName(existingAlbum.getName()))
								|| spotifyService.cleanAlbumName(existingAlbum
										.getName())
										.startsWith(
												spotifyService.cleanAlbumName(album.getName()))) {
							continue NEXTALBUM;
						}
					}
					if (!album.getArtist().getName().toLowerCase()
							.equals(artistName.toLowerCase())) {
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
			} catch (Throwable e) {
				LOG.error("Error processing albums for artist " + artistName, e);
			}
			addFinishedOperation();
		}
		if (files.size() > 0) {
			libraryAdditionDao.addFiles("spotify:", files);
			libraryAdditionDao.updateLibrary();
			
			albumInfoDao.createAlbumInfo(albumInfos);
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