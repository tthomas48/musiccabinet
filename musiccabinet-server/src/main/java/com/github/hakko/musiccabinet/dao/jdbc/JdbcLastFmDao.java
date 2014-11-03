package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.LastFmGroupRowMapper;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public class JdbcLastFmDao implements LastFmDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
        
        private final String GET_LASTFM_USER_ID="select * from music.get_lastfmuser_id(?)";
        
        private final String GET_LASTFM_USER="select id, lastfm_user_capitalization, session_key"
				+ " from music.lastfmuser where lastfm_user = upper(?)";
        
        private final String CREATE_LASTFM_USER="update music.lastfmuser set lastfm_user_capitalization = ?, session_key = ?	"
				+ " where id = ?";
        
        private final String GET_LASTFM_GROUP_ID="select * from music.get_lastfmgroup_id(?)";
        
        private final String SET_LASTFM_GROUPS="insert into music.lastfmgroup_import (group_name) values (?)";

        @Override
	public int getLastFmUserId(String lastFmUsername) {		
		return jdbcTemplate.queryForInt(GET_LASTFM_USER_ID,new Object[]{lastFmUsername});
	}

	@Override
	public LastFmUser getLastFmUser(String lastFmUsername) {
		return jdbcTemplate.queryForObject(GET_LASTFM_USER, new Object[]{lastFmUsername}, new RowMapper<LastFmUser>() {
			@Override
			public LastFmUser mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new LastFmUser(rs.getInt(1), rs.getString(2), rs.getString(3));
			}
		});
	}

	@Override
	public void createOrUpdateLastFmUser(LastFmUser user) {
		user.setId(getLastFmUserId(user.getLastFmUsername()));
                
		jdbcTemplate.update(CREATE_LASTFM_USER,new Object[]{user.getLastFmUsername(), user.getSessionKey(), user.getId()});
	}

	@Override
	public int getLastFmGroupId(String lastFmGroupName) {
		return jdbcTemplate.queryForInt(GET_LASTFM_GROUP_ID,new Object[]{lastFmGroupName} );
	}

	@Override
	public List<LastFmGroup> getLastFmGroups() {
		String sql = "select group_name_capitalization from music.lastfmgroup"
			+ " where enabled order by group_name";

		return jdbcTemplate.query(sql, new LastFmGroupRowMapper());
	}

	@Override
	public void setLastFmGroups(List<LastFmGroup> lastFmGroups) {
		jdbcTemplate.update("truncate music.lastfmgroup_import");
		
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), SET_LASTFM_GROUPS);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("group_name", Types.VARCHAR));
		for (LastFmGroup group : lastFmGroups) {
			batchUpdate.update(new Object[]{group.getName()});
		}
		batchUpdate.flush();
		
		jdbcTemplate.execute("select music.update_lastfmgroup()");
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