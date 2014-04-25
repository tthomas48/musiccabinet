package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_MB_ALBUM;
import static com.github.hakko.musiccabinet.service.MusicBrainzService.TYPE_ALBUM;
import static com.github.hakko.musiccabinet.service.MusicBrainzService.TYPE_EP;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.apache.commons.lang.StringUtils.reverse;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;
import com.github.hakko.musiccabinet.domain.model.music.MBRelease;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcMusicBrainzAlbumDaoTest {
	
	@Autowired
	private JdbcMusicDao musicDao;

	@Autowired
	private JdbcMusicBrainzAlbumDao albumDao;
	
	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Autowired
	private JdbcPlayCountDao playCountDao;
	
	@Autowired
	private JdbcLastFmDao lastFmDao;

	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	private static final String ARTIST = "Cult of Luna",
	TITLE1 = "The Beyond", MBID1 = "236316f7-c919-3986-918b-25e135ba8000", TYPE1 = "Album",
	TITLE2 = "Bodies / Recluse", MBID2 = "5484925b-884c-31d8-9c3e-2ef3824e6a5f", TYPE2 = "EP",
	TITLE3 = "Eternal Kingdom";
	private static final Integer YEAR1 = 2003, YEAR2 = 2006, YEAR3 = 2008;
	private static final String UNKNOWN = "[unknown]";
	private static final String USER = "User";
	
	private Artist artist;
	private MBRelease album1, album2;
	
	@Before
	public void prepareTestData() throws ApplicationException {
		PostgreSQLUtil.loadFunction(albumDao, UPDATE_MB_ALBUM);

		additionDao.getJdbcTemplate().execute("truncate music.artist cascade");
		additionDao.getJdbcTemplate().execute("truncate library.file cascade");

		artist = new Artist(ARTIST);
		musicDao.setArtistUri(artist);
		
		album1 = new MBRelease(MBID1, null, null, TITLE1, TYPE1, YEAR1, null);
		album2 = new MBRelease(MBID2, null, null, TITLE2, TYPE2, YEAR2, null);
		album1.setArtistUri(artist.getUri());
		album2.setArtistUri(artist.getUri());
		albumDao.createAlbums(Arrays.asList(album1, album2));
	}

	@Test
	public void createsAndRetrievesAlbums() {
		List<MBAlbum> albums = albumDao.getAlbums(artist.getUri());
		assertEquals(2, albums.size());
		assertEquals(album1.getTitle(), albums.get(0).getTitle());
		assertEquals(album2.getTitle(), albums.get(1).getTitle());
	}
	
	@Test
	public void findsAlbumsMissingFromLibrary() {
		List<MBAlbum> albums = albumDao.getMissingAlbums(null, null, null, -1, 0);
		
		assertEquals(2, albums.size());
		assertEquals(album1.getTitle(), albums.get(0).getTitle());
		assertEquals(album2.getTitle(), albums.get(1).getTitle());
		
		submitFile(additionDao, getFile(artist.getName(), album1.getTitle(), album1.getTitle()));

		albums = albumDao.getMissingAlbums(null, null, null, -1, 0);
		assertEquals(1, albums.size());
		assertEquals(album2.getTitle(), albums.get(0).getTitle());

	}

	@Test
	public void findsAlbumsMissingFromLibraryWithNameFilter() {
		// create artist with searchable name in library
		submitFile(additionDao, getFile(artist.getName(), UNKNOWN, UNKNOWN));

		String partialName = ARTIST.substring(ARTIST.lastIndexOf(' ') + 1);
		List<MBAlbum> albums = albumDao.getMissingAlbums(partialName, null, null, -1, 0);
		assertEquals(2, albums.size());
		
		albums = albumDao.getMissingAlbums(reverse(ARTIST), null, null, -1, 0);
		assertEquals(0, albums.size());
	}

	@Test
	public void findsAlbumsMissingFromLibraryWithRecentlyPlayedFilter() {
		List<MBAlbum> albums = albumDao.getMissingAlbums(null, null, USER, 10, 0);
		assertEquals(0, albums.size());
		
		LastFmUser lastFmUser = new LastFmUser(USER);
		lastFmDao.createOrUpdateLastFmUser(lastFmUser);

		submitFile(additionDao, getFile(artist.getName(), UNKNOWN, UNKNOWN));
		Track track = browserDao.getTracks(browserDao.getRandomTrackUris(1)).get(0);
		playCountDao.addPlayCount(lastFmUser, track);
		
		albums = albumDao.getMissingAlbums(null, null, USER, 10, 0);
		assertEquals(2, albums.size());
	}
	
	@Test
	public void findsAlbumsMissingFromLibraryWithTypeFilter() {
		List<MBAlbum> albums = albumDao.getMissingAlbums(null, asList(TYPE_ALBUM), null, -1, 0);
		assertEquals(1, albums.size());
		assertEquals(TITLE1, albums.get(0).getTitle());

		albums = albumDao.getMissingAlbums(null, asList(TYPE_EP), null, -1, 0);
		assertEquals(1, albums.size());
		assertEquals(TITLE2, albums.get(0).getTitle());
		
		albums = albumDao.getMissingAlbums(null, asList(TYPE_EP, TYPE_ALBUM), null, -1, 0);
		assertEquals(2, albums.size());
		assertEquals(TITLE1, albums.get(0).getTitle());
		assertEquals(TITLE2, albums.get(1).getTitle());
	}
	
	@Test
	public void findsAlbumsMissingFromLibraryWithPagination() {
		List<MBAlbum> albums = albumDao.getMissingAlbums(null, null, null, -1, 1);

		assertEquals(1, albums.size());
	}

	@Test
	public void detectsPresentDiscography() {
		Assert.assertTrue(albumDao.hasDiscography());

		additionDao.getJdbcTemplate().execute("truncate music.artist cascade");

		Assert.assertFalse(albumDao.hasDiscography());
	}

	@Test
	public void musicBrainzReleasesAreReturnedInDiscography() {
		List<Album> albums = albumDao.getDiscography(artist.getUri(), true, true);
		
		Assert.assertNotNull(albums);
		Assert.assertEquals(2, albums.size());
		
		Assert.assertEquals(YEAR1, albums.get(0).getYear());
		Assert.assertEquals(YEAR2, albums.get(1).getYear());
		
		Assert.assertEquals(TITLE1, albums.get(0).getName());
		Assert.assertEquals(TITLE2, albums.get(1).getName());

		Assert.assertEquals(new SubsonicUri(-1), albums.get(0).getUri());
		Assert.assertEquals(new SubsonicUri(-1), albums.get(1).getUri());

		Assert.assertTrue(albums.get(0).getTrackUris().isEmpty());
		Assert.assertTrue(albums.get(1).getTrackUris().isEmpty());
	}
	
	@Test
	public void mixesMusicBrainzReleasesAndLocalAlbumsIntoDiscography() {

		UnittestLibraryUtil.submitFile(additionDao, Arrays.asList(
				UnittestLibraryUtil.getFile(ARTIST, TITLE2, TITLE2, YEAR2),
				UnittestLibraryUtil.getFile(ARTIST, TITLE3, TITLE3, YEAR3)));
		
		List<Album> albums = albumDao.getDiscography(artist.getUri(), true, false);

		Assert.assertNotNull(albums);
		Assert.assertEquals(3, albums.size());

		Assert.assertEquals(YEAR3, albums.get(0).getYear());
		Assert.assertEquals(YEAR2, albums.get(1).getYear());
		Assert.assertEquals(YEAR1, albums.get(2).getYear());
		
		Assert.assertEquals(TITLE3, albums.get(0).getName());
		Assert.assertEquals(TITLE2, albums.get(1).getName());
		Assert.assertEquals(TITLE1, albums.get(2).getName());

		Assert.assertFalse(new SubsonicUri(-1).equals(albums.get(0).getUri()));
		Assert.assertFalse(new SubsonicUri(-1).equals(albums.get(1).getUri()));
		Assert.assertTrue(new SubsonicUri(-1).equals(albums.get(2).getUri()));

		Assert.assertTrue(albums.get(0).getTrackUris().isEmpty());
		Assert.assertTrue(albums.get(1).getTrackUris().isEmpty());
		Assert.assertTrue(albums.get(2).getTrackUris().isEmpty());
	}

}