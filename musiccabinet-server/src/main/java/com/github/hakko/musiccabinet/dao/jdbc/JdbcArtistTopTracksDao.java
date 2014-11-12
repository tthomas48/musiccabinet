package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.ArtistTopTracksDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.TrackRowMapper;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.TrackWithArtistRowMapper;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcArtistTopTracksDao implements ArtistTopTracksDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void createTopTracks(Artist artist, List<Track> topTracks) {
		if (topTracks.size() > 0) {
			clearImportTable();
			batchInsert(artist, topTracks);
			updateTopTracks();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.artisttoptrack_import");
	}

	private void batchInsert(Artist artist, List<Track> topTracks) {
		int sourceArtistId = jdbcTemplate.queryForInt(
				"select * from music.get_artist_id(?)", artist.getName());
		
		String sql = "insert into music.artisttoptrack_import (artist_id, track_name, rank) values (?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("track_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("rank", Types.SMALLINT));
		
		short rank = 0;
		for (Track t : topTracks) {
			batchUpdate.update(new Object[]{sourceArtistId, t.getName(), ++rank});
		}
		batchUpdate.flush();
	}

	private void updateTopTracks() {
		jdbcTemplate.execute("select music.update_artisttoptrack()");
	}
	
	@Override
	public List<Track> getTopTracks(Artist artist) {
		final int artistId = jdbcTemplate.queryForInt(
				"select * from music.get_artist_id(?)", artist.getName());
		
		String sql = "select artist_name_capitalization, track_name_capitalization"
			+ " from music.artisttoptrack att" 
			+ " inner join music.artist a on att.artist_id = a.id"
			+ " inner join music.track t on att.track_id = t.id"
			+ " where a.id = ? order by att.rank";
		
		return jdbcTemplate.query(sql, new Object[]{artistId}, 
				new TrackWithArtistRowMapper());
	}

	@Override
	public List<Track> getTopTracks(Uri artistUri) {
		return getTopTracks(artistUri, 20);
	}
	
	protected List<Track> getTopTracks(Uri artistUri, int limit) {
		
		String sql = "select coalesce(attpc.track_id, -1), t.track_name_capitalization"
				+ " from music.artisttoptrack att"
				+ " inner join music.track t on att.track_id = t.id"
				+ " left outer join library.artisttoptrackplaycount attpc"
				+ " on attpc.artist_id = att.artist_id and attpc.rank = att.rank"
				+ " where att.artist_id = ? order by att.rank limit ?";
		
		return jdbcTemplate.query(sql, new Object[]{artistUri.getId(), limit}, 
				new TrackRowMapper());
	}
	
	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	// Spring setters
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

}