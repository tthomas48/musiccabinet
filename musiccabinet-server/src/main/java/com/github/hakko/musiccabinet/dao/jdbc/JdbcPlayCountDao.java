package com.github.hakko.musiccabinet.dao.jdbc;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.PlayCountDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.UriRowMapper;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcPlayCountDao implements PlayCountDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
        
        private final String ADD_PLAY_COUNT="insert into library.playcount (lastfmuser_id, track_id, album_id, artist_id)"
				+ " select ?,track_id,?,? from library.track where id = ?";
        
        private final String GET_RECENT_ARTISTS="select artist_id from ("
				+ " select artist_id, max(invocation_time) as last_invocation_time "
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by artist_id"
				+ ") pc order by last_invocation_time desc offset ? limit ?";
        
        private final String GET_RECENT_ALBUMS="select album_id from ("
				+ " select album_id, max(invocation_time) as last_invocation_time "
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by album_id"
				+ ") pc order by last_invocation_time desc offset ? limit ?";
        
        private final String GET_RECENT_TRACKS="select lt.id from ("
				+ " select track_id, album_id, max(invocation_time) as last_invocation_time"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?) group by track_id, album_id"
				+ ") pc inner join library.track lt"
				+ " on lt.track_id = pc.track_id and lt.album_id = pc.album_id"
				+ " order by last_invocation_time desc offset ? limit ?";
        
        private final String GET_MOST_PLAYED_ARTISTS="select artist_id"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by artist_id"
				+ " order by count(artist_id) desc offset ? limit ?";
        
        private final String GET_MOST_PLAYED_ALBUMS="select album_id"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by album_id"
				+ " order by count(album_id) desc offset ? limit ?";
        
        
        private final String GET_MOST_PLAYED_TRACKS="select lt.id from library.track lt"
				+ " inner join ("
				+ " select track_id, album_id"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by track_id, album_id"
				+ " order by count(track_id) desc) pc"
				+ " on pc.album_id = lt.album_id and pc.track_id = lt.track_id"
				+ " offset ? limit ?";

	@Override
	public void addPlayCount(LastFmUser lastFmUser, Track track) {
		
		jdbcTemplate.update(ADD_PLAY_COUNT,new Object[]{lastFmUser.getId(), track.getMetaData().getAlbumUri(),
				track.getMetaData().getArtistUri(), track.getUri()} );
	}

	@Override
	public List<? extends Uri> getRecentArtists(String lastFmUser, int offset, int limit) {
		return jdbcTemplate.query(GET_RECENT_ARTISTS, new UriRowMapper(), lastFmUser, offset, limit);
	}

	@Override
	public List<? extends Uri> getRecentAlbums(String lastFmUser, int offset, int limit) {
		return jdbcTemplate.query(GET_RECENT_ALBUMS, new UriRowMapper(), lastFmUser, offset, limit);
	}

	@Override
	public List<? extends Uri> getRecentTracks(String lastFmUser, int offset, int limit) {
		return jdbcTemplate.query(GET_RECENT_TRACKS, new UriRowMapper(), lastFmUser, offset, limit);
	}

	@Override
	public List<? extends Uri> getMostPlayedArtists(String lastFmUser, int offset, int limit) {
		return jdbcTemplate.query(GET_MOST_PLAYED_ARTISTS, new UriRowMapper(), lastFmUser, offset, limit);
	}

	@Override
	public List<? extends Uri> getMostPlayedAlbums(String lastFmUser, int offset, int limit) {		
		return jdbcTemplate.query(GET_MOST_PLAYED_ALBUMS, new UriRowMapper(), lastFmUser, offset, limit);
	}

	@Override
	public List<? extends Uri> getMostPlayedTracks(String lastFmUser, int offset, int limit) {
		return jdbcTemplate.query(GET_MOST_PLAYED_TRACKS, new UriRowMapper(), lastFmUser, offset, limit);
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