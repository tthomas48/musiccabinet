package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.dao.SpotifyDao;
import com.github.hakko.musiccabinet.domain.model.library.SpotifyUser;

public class JdbcSpotifyDao implements SpotifyDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void createOrUpdateSpotifyUser(SpotifyUser user) {

		user.setId(getSpotifyUserId(user.getUserName()));

		Object[] params = new Object[] { user.getUserName(), user.getBlob(),
				user.getFullName(), user.getDisplayName(), user.getCountry(),
				user.getImageURL(), user.getId()};
		String sql = "update music.spotify_user set userName = ?, blob = ?, fullName = ?, displayName = ?, country = ?, imageURL = ? where id = ?";
		if(user.getId() < 0) {
			sql = "insert into music.spotify_user (userName, blob, fullName, displayName, country, imageURL) values (?, ?, ?, ?, ?, ?)";
			params = new Object[] { user.getUserName(), user.getBlob(),
					user.getFullName(), user.getDisplayName(), user.getCountry(),
					user.getImageURL()};
		}

		jdbcTemplate.update(sql, params);
	}

	@Override
	public SpotifyUser getSpotifyUser(String spotifyUsername) {
		String sql = "select id, userName, blob, fullName, displayName, country, imageURL from music.spotify_user where upper(userName) = upper(?) ";
		List<SpotifyUser> spotifyUsers =  jdbcTemplate.query(sql,
				new String[] { spotifyUsername }, new RowMapper<SpotifyUser>() {
					@Override
					public SpotifyUser mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return new SpotifyUser(rs.getInt(1), rs.getString(2),
								rs.getString(3), rs.getString(4), rs
										.getString(5), rs.getString(6), rs
										.getString(7));
					}
				});
		if(spotifyUsers.size() == 1) {
			return spotifyUsers.get(0);
		}
		return null;
	}

	@Override
	public int getSpotifyUserId(String spotifyName) {

		try {
			String sql = "select id from music.spotify_user where upper(userName) = upper(?)";

			return jdbcTemplate.queryForInt(sql, spotifyName);
		} catch (Exception e) {
			return -1;
		}
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