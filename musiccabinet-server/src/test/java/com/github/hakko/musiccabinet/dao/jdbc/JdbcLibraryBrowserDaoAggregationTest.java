package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.ADD_TO_LIBRARY;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcLibraryBrowserDaoAggregationTest {
	
	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	@Autowired
	private JdbcLibraryAdditionDao additionDao;

	@Autowired
	private JdbcPlayCountDao playCountDao;

	@Autowired
	private JdbcLastFmDao lastFmDao;

	@Autowired
	private JdbcArtistInfoDao artistInfoDao;
	
	@Autowired
	private JdbcArtistTopTagsDao topTagsDao;

	@Autowired
	private JdbcStarDao starDao;
	
	private String userName1 = "rj", userName2 = "joanofarctan";
	private LastFmUser user1, user2;
	private String artistName1 = "Madonna", artistName2 = "Kylie Minogue";
	private String albumName1 = "Fever", albumName2 = "Like A Virgin";
	private String trackName1 = "Title";
	private Artist artist1;
	private Album album1, album2;
	private Track track1, track2;
	
	@Before
	public void prepareTestData() throws ApplicationException {
		PostgreSQLUtil.loadFunction(browserDao, ADD_TO_LIBRARY);
		
		browserDao.getJdbcTemplate().execute("truncate music.artist cascade");
		browserDao.getJdbcTemplate().execute("truncate library.file cascade");

		submitFile(additionDao, UnittestLibraryUtil.getFile(artistName1, albumName1, trackName1));
		submitFile(additionDao, UnittestLibraryUtil.getFile(artistName1, albumName2, trackName1));

		List<Artist> artists = browserDao.getArtists();
		Assert.assertEquals(1, artists.size());
		artist1 = artists.get(0);
		
		List<Album> albums = new ArrayList<Album>();
		browserDao.getAlbums(albums, artists.get(0), true);
		Assert.assertEquals(2, albums.size());
		Assert.assertEquals(1, albums.get(0).getTrackUris().size());
		Assert.assertEquals(1, albums.get(1).getTrackUris().size());
		Collections.sort(albums, getAlbumComparator());
		
		album1 = albums.get(0);
		album2 = albums.get(1);
		track1 = browserDao.getTracks(album1.getTrackUris()).get(0);
		track2 = browserDao.getTracks(album2.getTrackUris()).get(0);
		
		user1 = new LastFmUser(userName1);
		lastFmDao.createOrUpdateLastFmUser(user1);

		user2 = new LastFmUser(userName2);
		lastFmDao.createOrUpdateLastFmUser(user2);

		artistInfoDao.createArtistInfo(asList(new ArtistInfo(new Artist(artistName1), "img")));
	}
	
	private Comparator<Album> getAlbumComparator() {
		return new Comparator<Album>() {
			@Override
			public int compare(Album a1, Album a2) {
				return a1.getName().compareTo(a2.getName());
			}
		};
	}

	@Test
	public void findsArtistAndTrackNameByLibraryTrackId() {
		Track t1 = browserDao.getTracks(album1.getTrackUris()).get(0);
		Track t2 = browserDao.getTrack(album1.getTrackUris().get(0));

		assertEquals(t1.getArtist().getName(), t2.getArtist().getName());
		assertEquals(t1.getName(), t2.getName());
	}

	@Test
	public void filterArtistsByGenre() {
		String artist = artistName1, indieArtist = "Indie Artist", jazzArtist = "Jazz Artist";
		String indie = "indie", jazz = "jazz";
		submitFile(additionDao, UnittestLibraryUtil.getFile(artist, "Album", "Title"));
		submitFile(additionDao, UnittestLibraryUtil.getFile(indieArtist, "Album", "Title"));
		submitFile(additionDao, UnittestLibraryUtil.getFile(jazzArtist, "Album", "Title"));

		topTagsDao.createTopTags(new Artist(indieArtist), 
				Arrays.asList(new Tag("indie", (short) 100)));
		topTagsDao.createTopTags(new Artist(jazzArtist), 
				Arrays.asList(new Tag("jazz", (short) 60)));

		List<Artist> allArtists = browserDao.getArtists();
		Assert.assertEquals(3, allArtists.size());

		List<Artist> indieArtists = browserDao.getArtists(indie, 90);
		Assert.assertEquals(1, indieArtists.size());
		Assert.assertEquals(indieArtist, indieArtists.get(0).getName());

		List<Artist> jazzArtists = browserDao.getArtists(jazz, 70);
		Assert.assertEquals(0, jazzArtists.size());
	}
	
	@Test
	public void filtersArtistByFirstLetter() {
		String e = "efg", f1 = "FGH", f2 = "Fhi", る = "るり";
		submitFile(additionDao, UnittestLibraryUtil.getFile(artistName1, "Album", "Title"));
		submitFile(additionDao, UnittestLibraryUtil.getFile(e, "Album", "Title"));
		submitFile(additionDao, UnittestLibraryUtil.getFile(f1, "Album", "Title"));
		submitFile(additionDao, UnittestLibraryUtil.getFile(f2, "Album", "Title"));
		submitFile(additionDao, UnittestLibraryUtil.getFile(る, "Album", "Title"));
		
		List<Integer> indexes = browserDao.getArtistIndexes();
		Collections.sort(indexes);
		Assert.assertEquals(4, indexes.size());
		Assert.assertEquals('#', indexes.get(0).intValue());
		Assert.assertEquals('E', indexes.get(1).intValue());
		Assert.assertEquals('F', indexes.get(2).intValue());
		Assert.assertEquals('M', indexes.get(3).intValue());
		
		Assert.assertEquals(1, browserDao.getArtists('E').size());
		Assert.assertEquals(2, browserDao.getArtists('F').size());
		Assert.assertEquals(0, browserDao.getArtists('G').size());
		Assert.assertEquals(1, browserDao.getArtists('M').size());
		Assert.assertEquals(1, browserDao.getArtists('0').size());
		Assert.assertEquals(1, browserDao.getArtists('#').size());
		Assert.assertEquals(1, browserDao.getArtists('~').size());
	}

	@Test
	public void returnsRecentlyAddedAlbums() {
		submitFile(additionDao, UnittestLibraryUtil.getFile(artistName1, albumName1, trackName1));
		submitFile(additionDao, UnittestLibraryUtil.getFile(artistName1, albumName2, trackName1));
		
		List<Album> albums = browserDao.getRecentlyAddedAlbums(0, 10, null);
		Assert.assertNotNull(albums);
		Assert.assertEquals(2, albums.size());

		albums = browserDao.getRecentlyAddedAlbums(0, 1, null);
		Assert.assertNotNull(albums);
		Assert.assertEquals(1, albums.size());
		Assert.assertEquals(albumName2, albums.get(0).getName());

		albums = browserDao.getRecentlyAddedAlbums(1, 1, null);
		Assert.assertNotNull(albums);
		Assert.assertEquals(1, albums.size());
		Assert.assertEquals(albumName1, albums.get(0).getName());

		albums = browserDao.getRecentlyAddedAlbums(0, 10, albumName1);
		Assert.assertNotNull(albums);
		Assert.assertEquals(1, albums.size());
		Assert.assertEquals(albumName1, albums.get(0).getName());
	}
	
	@Test
	public void returnsRecentlyPlayedArtists() {
		List<ArtistRecommendation> recentlyPlayed = 
				browserDao.getRecentlyPlayedArtists(userName1, true, 0, 10, null);
		Assert.assertEquals(0, recentlyPlayed.size());
		
		playCountDao.addPlayCount(user1, track1);
		playCountDao.addPlayCount(user2, track2);

		assertEquals(1, browserDao.getRecentlyPlayedArtists(userName1, true, 0, 10, null).size());
		assertEquals(0, browserDao.getRecentlyPlayedArtists(userName1, true, 0, 10, artistName2).size());
		assertEquals(1, browserDao.getRecentlyPlayedArtists(userName1, true, 0, 10, artistName1).size());

		assertEquals(1, browserDao.getRecentlyPlayedArtists(null, true, 0, 10, null).size());
	}

	@Test
	public void returnsRecentlyPlayedAlbums() {
		List<Album> recentlyPlayed = browserDao.getRecentlyPlayedAlbums(userName1, 0, 10, null);
		Assert.assertEquals(0, recentlyPlayed.size());
		
		playCountDao.addPlayCount(user1, track1);
		assertEquals(1, browserDao.getRecentlyPlayedAlbums(userName1, 0, 10, null).size());
		assertEquals(0, browserDao.getRecentlyPlayedAlbums(userName1, 0, 10, albumName2).size());
		assertEquals(1, browserDao.getRecentlyPlayedAlbums(userName1, 0, 10, albumName1).size());
		assertEquals(1, browserDao.getRecentlyPlayedAlbums(null, 0, 10, null).size());

		playCountDao.addPlayCount(user1, track2);
		assertEquals(2, browserDao.getRecentlyPlayedAlbums(userName1, 0, 10, null).size());
		assertEquals(1, browserDao.getRecentlyPlayedAlbums(userName1, 0, 10, albumName2).size());
		assertEquals(1, browserDao.getRecentlyPlayedAlbums(userName1, 0, 10, albumName1).size());
		assertEquals(2, browserDao.getRecentlyPlayedAlbums(null, 0, 10, null).size());

		assertEquals(1, browserDao.getRecentlyPlayedAlbums(userName1, 1, 1, null).size());
		assertEquals(album2.getName(), browserDao.getRecentlyPlayedAlbums(userName1, 0, 1, null).get(0).getName());
		assertEquals(album1.getName(), browserDao.getRecentlyPlayedAlbums(userName1, 1, 1, null).get(0).getName());
		
		assertEquals(0, browserDao.getRecentlyPlayedAlbums(userName2, 0, 10, null).size());

		playCountDao.addPlayCount(user2, track2);
		assertEquals(1, browserDao.getRecentlyPlayedAlbums(userName2, 0, 10, null).size());
		assertEquals(2, browserDao.getRecentlyPlayedAlbums(null, 0, 10, null).size());
	}

	@Test
	public void returnsRecentlyPlayedTracks() {
		List<? extends Uri> recentlyPlayed = 
				browserDao.getRecentlyPlayedTrackUris(userName1, 0, 10, null);
		Assert.assertEquals(0, recentlyPlayed.size());

		playCountDao.addPlayCount(user1, track1);
		assertEquals(1, browserDao.getRecentlyPlayedTrackUris(userName1, 0, 10, null).size());
		assertEquals(0, browserDao.getRecentlyPlayedTrackUris(userName1, 0, 10, artistName2).size());
		assertEquals(0, browserDao.getRecentlyPlayedTrackUris(userName1, 0, 10, albumName2).size());
		assertEquals(1, browserDao.getRecentlyPlayedTrackUris(userName1, 0, 10, artistName1).size());
		assertEquals(1, browserDao.getRecentlyPlayedTrackUris(userName1, 0, 10, albumName1).size());

		playCountDao.addPlayCount(user2, track2);
		assertEquals(1, browserDao.getRecentlyPlayedTrackUris(userName2, 0, 10, null).size());
		assertEquals(2, browserDao.getRecentlyPlayedTrackUris(null, 0, 10, null).size());
	}

	@Test
	public void returnsMostPlayedArtists() {
		List<ArtistRecommendation> mostPlayed = 
				browserDao.getMostPlayedArtists(userName1, 0, 10, null);
		Assert.assertEquals(0, mostPlayed.size());
		
		playCountDao.addPlayCount(user1, track1);
		assertEquals(1, browserDao.getMostPlayedArtists(userName1, 0, 10, null).size());
		assertEquals(0, browserDao.getMostPlayedArtists(userName1, 0, 10, artistName2).size());
		assertEquals(1, browserDao.getMostPlayedArtists(userName1, 0, 10, artistName1).size());
	}

	@Test
	public void returnsMostPlayedAlbums() {
		List<Album> mostPlayed = browserDao.getMostPlayedAlbums(userName1, 0, 10, null);
		Assert.assertEquals(0, mostPlayed.size());
		
		playCountDao.addPlayCount(user1, track1);
		assertEquals(1, browserDao.getMostPlayedAlbums(userName1, 0, 10, null).size());
		assertEquals(0, browserDao.getMostPlayedAlbums(userName1, 0, 10, albumName2).size());
		assertEquals(1, browserDao.getMostPlayedAlbums(userName1, 0, 10, albumName1).size());
		assertEquals(1, browserDao.getMostPlayedAlbums(null, 0, 10, null).size());

		playCountDao.addPlayCount(user1, track2);
		playCountDao.addPlayCount(user1, track2);
		assertEquals(2, browserDao.getMostPlayedAlbums(userName1, 0, 10, null).size());
		assertEquals(1, browserDao.getMostPlayedAlbums(userName1, 0, 10, albumName2).size());
		assertEquals(1, browserDao.getMostPlayedAlbums(userName1, 0, 10, albumName1).size());

		assertEquals(1, browserDao.getMostPlayedAlbums(userName1, 1, 1, null).size());
		assertEquals(album2.getName(), browserDao.getMostPlayedAlbums(userName1, 0, 1, null).get(0).getName());
		assertEquals(album1.getName(), browserDao.getMostPlayedAlbums(userName1, 1, 1, null).get(0).getName());

		assertEquals(0, browserDao.getMostPlayedAlbums(userName2, 0, 10, null).size());
		assertEquals(2, browserDao.getMostPlayedAlbums(null, 0, 10, null).size());

		playCountDao.addPlayCount(user2, track1);
		assertEquals(1, browserDao.getMostPlayedAlbums(userName2, 0, 10, null).size());
		assertEquals(2, browserDao.getMostPlayedAlbums(null, 0, 10, null).size());
	}

	@Test
	public void returnsMostPlayedTracks() {
		List<? extends Uri> mostPlayed = 
				browserDao.getMostPlayedTrackUris(userName1, 0, 10, null);
		Assert.assertEquals(0, mostPlayed.size());

		playCountDao.addPlayCount(user1, track1);
		playCountDao.addPlayCount(user2, track2);
		assertEquals(1, browserDao.getMostPlayedTrackUris(userName1, 0, 10, null).size());
		assertEquals(0, browserDao.getMostPlayedTrackUris(userName1, 0, 10, artistName2).size());
		assertEquals(0, browserDao.getMostPlayedTrackUris(userName1, 0, 10, albumName2).size());
		assertEquals(1, browserDao.getMostPlayedTrackUris(userName1, 0, 10, artistName1).size());
		assertEquals(1, browserDao.getMostPlayedTrackUris(userName1, 0, 10, albumName1).size());

		assertEquals(1, browserDao.getMostPlayedTrackUris(userName2, 0, 10, null).size());
		assertEquals(2, browserDao.getMostPlayedTrackUris(null, 0, 10, null).size());
	}

	@Test
	public void returnsRandomArtist() {
		Assert.assertEquals(0, browserDao.getRandomArtists(true, 0).size());
		Assert.assertEquals(1, browserDao.getRandomArtists(true, 1).size());
	}

	@Test
	public void returnsRandomAlbum() {
		Assert.assertEquals(0, browserDao.getRandomAlbums(0).size());
		Assert.assertEquals(1, browserDao.getRandomAlbums(1).size());
		Assert.assertEquals(2, browserDao.getRandomAlbums(2).size());
	}

	@Test
	public void returnsRandomTrackIds() {
		Assert.assertEquals(0, browserDao.getRandomTrackUris(0).size());
		Assert.assertEquals(1, browserDao.getRandomTrackUris(1).size());
		Assert.assertEquals(2, browserDao.getRandomTrackUris(2).size());
	}

	@Test
	public void returnsRandomTrackIdsWithCriteria() {
		browserDao.getRandomTrackUris(1, null, null, null);

		browserDao.getRandomTrackUris(1, 2000, null, null);
		browserDao.getRandomTrackUris(1, null, 2010, null);
		browserDao.getRandomTrackUris(1, null, null, "sludge");

		browserDao.getRandomTrackUris(2, 2005, 2005, null);
		browserDao.getRandomTrackUris(2, 2006, 2004, "disco");
	}

	@Test
	public void returnsStarredArtist() {
		starDao.starArtist(user1, artist1.getUri());

		Assert.assertEquals(1, browserDao.getStarredArtists(userName1, 0, 10, null).size());
		Assert.assertEquals(1, browserDao.getStarredArtists(userName1, 0, 10, artistName1).size());
		Assert.assertEquals(0, browserDao.getStarredArtists(userName1, 0, 10, artistName2).size());
		Assert.assertEquals(0, browserDao.getStarredArtists(userName2, 0, 10, artistName1).size());
		Assert.assertEquals(0, browserDao.getStarredArtists(userName1, 1, 10, artistName1).size());
		Assert.assertEquals(1, browserDao.getStarredArtists(null, 0, 10, null).size());

		starDao.starArtist(user2, artist1.getUri());
		Assert.assertEquals(1, browserDao.getStarredArtists(userName2, 0, 10, null).size());
		Assert.assertEquals(2, browserDao.getStarredArtists(null, 0, 10, null).size());
	}
	
	@Test
	public void returnsStarredAlbum() {
		starDao.starAlbum(user1, album1.getUri());
		
		Assert.assertEquals(1, browserDao.getStarredAlbums(userName1, 0, 10, null).size());
		Assert.assertEquals(1, browserDao.getStarredAlbums(userName1, 0, 10, albumName1).size());
		Assert.assertEquals(0, browserDao.getStarredAlbums(userName1, 0, 10, albumName2).size());
		Assert.assertEquals(0, browserDao.getStarredAlbums(userName2, 0, 10, albumName1).size());
		Assert.assertEquals(0, browserDao.getStarredAlbums(userName1, 1, 10, albumName1).size());
		Assert.assertEquals(1, browserDao.getStarredAlbums(null, 0, 10, null).size());

		starDao.starAlbum(user2, album2.getUri());
		Assert.assertEquals(1, browserDao.getStarredAlbums(userName2, 0, 10, null).size());
		Assert.assertEquals(2, browserDao.getStarredAlbums(null, 0, 10, null).size());
	}

	@Test
	public void returnsStarredTrackIds() {
		starDao.starTrack(user1, track1.getUri());
		
		Assert.assertEquals(1, browserDao.getStarredTrackUris(userName1, 0, 10, null).size());
		Assert.assertEquals(1, browserDao.getStarredTrackUris(userName1, 0, 10, albumName1).size());
		Assert.assertEquals(0, browserDao.getStarredTrackUris(userName1, 0, 10, albumName2).size());
		Assert.assertEquals(0, browserDao.getStarredTrackUris(userName2, 0, 10, albumName1).size());
		Assert.assertEquals(0, browserDao.getStarredTrackUris(userName1, 1, 10, albumName1).size());
		Assert.assertEquals(1, browserDao.getStarredTrackUris(null, 0, 10, null).size());

		starDao.starTrack(user2, track2.getUri());
		Assert.assertEquals(1, browserDao.getStarredTrackUris(userName2, 0, 10, null).size());
		Assert.assertEquals(2, browserDao.getStarredTrackUris(null, 0, 10, null).size());
	}

}