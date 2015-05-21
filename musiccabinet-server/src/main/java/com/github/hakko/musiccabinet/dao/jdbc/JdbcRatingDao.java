package com.github.hakko.musiccabinet.dao.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.RatingDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.AlbumRowMapper;
import com.github.hakko.musiccabinet.domain.model.music.Album;

public class JdbcRatingDao implements RatingDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public List<Album> getHighestRatedAlbums(int offset, int limit) {

		if (limit < 1) {
			return Collections.emptyList();
		}

		List<Object> args = new ArrayList<>();
		args.add(offset);
		args.add(limit);

		String sql = "select ma.artist_id, a.artist_name_capitalization, ma.id, ma.album_name_capitalization, la.year,"
				+ " d1.path, f1.filename, d2.path, f2.filename, ai.extralargeimageurl, tr.track_ids, ma.spotify_uri, coalesce(ur.rating, 0) from"
				+ " (select lt.album_id as album_id, array_agg(lt.id order by coalesce(ft.disc_nr, 1)*100 + coalesce(ft.track_nr, 0)) as track_ids"
				+ " from library.track lt"
				+ " inner join music.album ma on lt.album_id = ma.id"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id"
				+ " group by lt.album_id) tr"
				+ " left outer join library.user_rating ur on ur.id = tr.album_id"
				+ " inner join library.album la on la.album_id = tr.album_id"
				+ " inner join music.album ma on la.album_id = ma.id"
				+ " inner join music.artist a on ma.artist_id = a.id"
				+ " left outer join library.file f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join library.directory d1 on f1.directory_id = d1.id"
				+ " left outer join library.file f2 on f2.id = la.coverartfile_id"
				+ " left outer join library.directory d2 on f2.directory_id = d2.id"
				+ " left outer join music.albuminfo ai on ai.album_id = la.album_id"
				+ " order by avg(coalesce(ur.rating, 0)) desc"
				+ " offset ? limit ?";

		return jdbcTemplate.query(sql, args.toArray(), new AlbumRowMapper());

	}

	@Override
	public Double getAverageRating(Uri uri) {
		try {
			return (Double) getJdbcTemplate().queryForObject(
					"select avg(rating) from library.user_rating where id = ?",
					new Object[] { uri.getId() }, Double.class);
		} catch (EmptyResultDataAccessException x) {
			return null;
		}

	}

	@Override
	public Integer getRatingForUser(String username, Uri uri) {
		try {
			return getJdbcTemplate().queryForInt(
					"select rating from library.user_rating where username=? and id=?",
					new Object[] { username, uri.getId() });
		} catch (EmptyResultDataAccessException x) {
			return null;
		}
	}

	@Override
	public int getRatedAlbumCount(String username) {
		Map<String, Object> args = new HashMap<String, Object>();

		return getJdbcTemplate().queryForInt(
				"select count(distinct(lt.album_id)) from library.user_rating ur, "
						+ "library.track lt " + "where lt.id = ur.id "
						+ "and ur.username = ?", username);
	}

	@Override
	public void setRatingForUser(String username, Uri uri, Integer rating) {
		if (rating != null && (rating < 1 || rating > 5)) {
			return;
		}

		getJdbcTemplate().update(
				"delete from library.user_rating where username=? and id=?", username,
				uri.getId());
		if (rating != null) {
			getJdbcTemplate().update("insert into library.user_rating values(?, ?, ?)",
					username, uri.getId(), rating);
		}

	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

}
