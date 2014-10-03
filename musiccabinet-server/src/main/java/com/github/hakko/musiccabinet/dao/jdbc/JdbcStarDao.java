package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.jdbc.JdbcNameSearchDao.getNameQuery;
import static java.lang.Short.MAX_VALUE;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.StarDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.UriRowMapper;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public class JdbcStarDao implements StarDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
        private final String STAR_ARTIST="insert into library.starredartist (lastfmuser_id, artist_id)"
				+ " select ?,? where not exists (select 1 from library.starredartist"
				+ " where lastfmuser_id = ? and artist_id = ?)";
        
        private final String UNSTAR_ARTIST="delete from library.starredartist where lastfmuser_id = ? and artist_id = ?";

        private final String STAR_ALBUM="insert into library.starredalbum (lastfmuser_id, album_id)"
				+ " select ?,? where not exists (select 1 from library.starredalbum"
				+ " where lastfmuser_id = ? and album_id = ?)";
        
        private final String UNSTAR_ALBUM="delete from library.starredalbum where lastfmuser_id = ? and album_id = ?";
        
        private final String STAR_TRACK="insert into library.starredtrack (lastfmuser_id, album_id, track_id)"
				+ " select ?, lt.album_id, lt.track_id from library.track lt"
				+ " where lt.id = ? and not exists ("
				+ " select 1 from library.starredtrack st"
				+ " inner join library.track lt"
				+ "  on st.album_id = lt.album_id and st.track_id = lt.track_id"
				+ "  where st.lastfmuser_id = ? and lt.id = ?)";
        
        private final String UNSTAR_TRACK="delete from library.starredtrack st"
				+ " using library.track lt"
				+ " where lt.album_id = st.album_id and lt.track_id = st.track_id"
				+ " and lt.id = ?  and lastfmuser_id = ? ";
        
	@Override
	public void starArtist(LastFmUser lastFmUser, Uri artistUri) {
		jdbcTemplate.update(STAR_ARTIST,new Object[]{ lastFmUser.getId(), artistUri.getId(), lastFmUser.getId(), artistUri.getId()});
	}

	@Override
	public void unstarArtist(LastFmUser lastFmUser, Uri artistUri) {
		jdbcTemplate.update(UNSTAR_ARTIST,new Object[]{lastFmUser.getId(), artistUri.getId()});
	}

	@Override
	public List<? extends Uri> getStarredArtistUris(LastFmUser lastFmUser, int offset, int limit, String query) {
		String sql = "select sa.artist_id from library.starredartist sa"
				+ " inner join library.artist la on sa.artist_id = la.artist_id"
				+ " where sa.lastfmuser_id = ?"
				+ (query == null ? "" : " and la.artist_name_search like ?")
				+ " order by added desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUser.getId(), offset, limit} : 
				new Object[]{lastFmUser.getId(), getNameQuery(query), offset, limit};
		return jdbcTemplate.query(sql, params, new UriRowMapper());
	}

	@Override
	public void starAlbum(LastFmUser lastFmUser, Uri albumUri) {
		jdbcTemplate.update(STAR_ALBUM,new Object[]{lastFmUser.getId(), albumUri.getId(), lastFmUser.getId(), albumUri.getId()} );
	}

	@Override
	public void unstarAlbum(LastFmUser lastFmUser, Uri albumUri) {
		jdbcTemplate.update(UNSTAR_ALBUM, new Object[]{lastFmUser.getId(), albumUri.getId()} );
	}

	@Override
	public List<? extends Uri> getStarredAlbumUris(LastFmUser lastFmUser, int offset, int limit, String query) {
		String sql = "select sa.album_id from library.starredalbum sa"
				+ " inner join library.album la on sa.album_id = la.album_id"
				+ " where sa.lastfmuser_id = ?"
				+ (query == null ? "" : " and la.album_name_search like ?")
				+ " order by added desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUser.getId(), offset, limit} : 
				new Object[]{lastFmUser.getId(), getNameQuery(query), offset, limit};
		return jdbcTemplate.query(sql, params, new UriRowMapper());
	}

	@Override
	public void starTrack(LastFmUser lastFmUser, Uri trackUri) {
		jdbcTemplate.update(STAR_TRACK,new Object[]{lastFmUser.getId(), trackUri.getId(), lastFmUser.getId(), trackUri.getId()} );
	}

	@Override
	public void unstarTrack(LastFmUser lastFmUser, Uri trackUri) {
		jdbcTemplate.update(UNSTAR_TRACK,new Object[]{trackUri.getId(),lastFmUser.getId()});
	}

	public List<? extends Uri> getStarredTrackUris(LastFmUser lastFmUser) {
		return getStarredTrackUris(lastFmUser, 0, MAX_VALUE, null);
	}

	@Override
	public List<? extends Uri> getStarredTrackUris(LastFmUser lastFmUser, int offset, int limit, String query) {
		String sql = "select lt.id from library.starredtrack st"
				+ " inner join library.track lt on st.album_id = lt.album_id"
				+ "  and st.track_id = lt.track_id"
				+ " where st.lastfmuser_id = ?"
				+ (query == null ? "" : " and lt.track_name_search like ?")
				+ " order by added desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUser.getId(), offset, limit} : 
				new Object[]{lastFmUser.getId(), getNameQuery(query), offset, limit};
		return jdbcTemplate.query(sql, params, new UriRowMapper());
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