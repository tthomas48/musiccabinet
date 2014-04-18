package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_AVAILABLE_TOP_TRACKS;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil.loadFunction;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil.truncateTables;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.ArtistRelationDao;
import com.github.hakko.musiccabinet.dao.ArtistTopTracksDao;
import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.TrackRelationDao;
import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistSimilarityParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistSimilarityParserImpl;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksParserImpl;
import com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityParser;
import com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcPlaylistGeneratorDaoTest {

	@Autowired
	private JdbcPlaylistGeneratorDao playlistGeneratorDao;

	@Autowired
	private ArtistRelationDao artistRelationDao;
	
	@Autowired
	private ArtistTopTracksDao artistTopTracksDao;
	
	@Autowired
	private TrackRelationDao trackRelationDao;
	
	@Autowired
	private MusicDao musicDao;
	
	@Autowired
	private LibraryAdditionDao additionDao;
	
	private static final String CHER_SIMILAR_ARTISTS = 
		"last.fm/xml/similarartists.cher.xml";
	private static final String CHER_TOP_TRACKS = 
		"last.fm/xml/toptracks.cher.xml";
	private static final String CHER_SIMILAR_TRACKS = 
		"last.fm/xml/similartracks.cher.believe.xml";

	@Before
	public void clearTopTracksAndRelations() throws ApplicationException {
		loadFunction(playlistGeneratorDao, UPDATE_AVAILABLE_TOP_TRACKS);

		truncateTables(playlistGeneratorDao);
	}

	@Test
	public void preparingTestdataCreatesSearchIndex() throws ApplicationException {
		prepareTestdataForArtist();
		
		Assert.assertTrue(playlistGeneratorDao.isSearchIndexCreated());
	}
	
	@Test
	public void addCherRelationsAndTopTracksAndGetPlaylist() throws ApplicationException {
		Uri artistUri = prepareTestdataForArtist();
		
		List<PlaylistItem> ts = playlistGeneratorDao.getPlaylistForArtist(artistUri, 3, 20);
		
		Assert.assertNotNull(ts);
		Assert.assertEquals(3, ts.size());
	}
	
	@Test
	public void getTopTracks() throws ApplicationException {
		Uri artistUri = prepareTestdataForArtist();
		
		List<? extends Uri> ts = playlistGeneratorDao.getTopTracksForArtist(artistUri, 20);
		
		Assert.assertNotNull(ts);
		Assert.assertEquals(20, ts.size());
	}

	@Test
	public void getRelatedArtists() throws ApplicationException {
		Uri artistUri = prepareTestdataForArtist();
		
		List<? extends Uri> ts = playlistGeneratorDao.getPlaylistForRelatedArtists(artistUri, 3, 20);
		
		Assert.assertNotNull(ts);
	}
	
	@Test
	public void addCherSimilarTracksAndGetPlaylist() throws ApplicationException {
		TrackSimilarityParser tsParser = new TrackSimilarityParserImpl(
				new ResourceUtil(CHER_SIMILAR_TRACKS).getInputStream());
		trackRelationDao.createTrackRelations(
				tsParser.getTrack(), tsParser.getTrackRelations());
		
		List<File> files = new ArrayList<>();
		for (TrackRelation tr : tsParser.getTrackRelations()) {
			String artistName = tr.getTarget().getArtist().getName();
			if ("Madonna".equals(artistName)) {
				files.add(getFile(artistName, null, tr.getTarget().getName()));
			}
		}
		UnittestLibraryUtil.submitFile(additionDao, files);
		
		Uri trackId = musicDao.getTrackUri(tsParser.getTrack().getArtist().getName(), 
				tsParser.getTrack().getName());
		
		playlistGeneratorDao.updateSearchIndex();
		List<PlaylistItem> ts = playlistGeneratorDao.getPlaylistForTrack(trackId);
		
		Assert.assertNotNull(ts);
		Assert.assertEquals(2, ts.size());
	}
	
	@Test
	public void getGroupPlaylist() {
		List<PlaylistItem> ts = playlistGeneratorDao.getPlaylistForGroup("unknown group", 3, 20);
		
		Assert.assertNotNull(ts);
	}
	
	private Uri prepareTestdataForArtist() throws ApplicationException {
		ArtistSimilarityParser asParser = new ArtistSimilarityParserImpl(
				new ResourceUtil(CHER_SIMILAR_ARTISTS).getInputStream());
		artistRelationDao.createArtistRelations(
				asParser.getArtist(), asParser.getArtistRelations());

		ArtistTopTracksParser attParser = new ArtistTopTracksParserImpl(
				new ResourceUtil(CHER_TOP_TRACKS).getInputStream());
		artistTopTracksDao.createTopTracks(
				attParser.getArtist(), attParser.getTopTracks());
		
		List<File> files = new ArrayList<>();
		for (Track topTrack : attParser.getTopTracks()) {
			files.add(UnittestLibraryUtil.getFile(topTrack));
		}
		UnittestLibraryUtil.submitFile(additionDao, files);

		Uri artistUri = musicDao.getArtistUri(asParser.getArtist());
		
		playlistGeneratorDao.updateSearchIndex();

		return artistUri;
	}

}