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

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.ArtistInfoDao;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.log.Logger;

public class JdbcArtistInfoDao implements ArtistInfoDao, JdbcTemplateDao {

	private static final Logger LOG = Logger.getLogger(JdbcArtistInfoDao.class);

	private JdbcTemplate jdbcTemplate;

	/**
	 * SQL Query Strings
	 */
	@Override
	public void createArtistInfo(List<ArtistInfo> artistInfos) {
		if (artistInfos.size() > 0) {
			clearImportTable();
			batchInsert(artistInfos);
			updateLibrary();
		}
	}

	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.artistinfo_import");
	}

	private void batchInsert(List<ArtistInfo> artistInfos) {

		String sql = "insert into music.artistinfo_import (artist_name, smallimageurl, mediumimageurl, largeimageurl, extralargeimageurl, listeners, playcount, biosummary, biocontent) values (?,?,?,?,?,?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(
				jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_name",
				Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("smallImageUrl",
				Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("mediumImageUrl",
				Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("largeImageUrl",
				Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("extraLargeImageUrl",
				Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("listeners",
				Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("playcount",
				Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("biosummary",
				Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("biocontent",
				Types.VARCHAR));

		for (ArtistInfo ai : artistInfos) {
			batchUpdate.update(new Object[] { ai.getArtist().getName(),
					ai.getSmallImageUrl(), ai.getMediumImageUrl(),
					ai.getLargeImageUrl(), ai.getExtraLargeImageUrl(),
					ai.getListeners(), ai.getPlayCount(), ai.getBioSummary(),
					ai.getBioContent() });
		}
		batchUpdate.flush();

	}

	private void updateLibrary() {
		jdbcTemplate.execute("select music.update_artistinfo()");
	}

	@Override
	public ArtistInfo getArtistInfo(final Uri artistUri) {
		final Integer artistId = artistUri.getId();

		String sql = "select a.artist_name_capitalization, ai.largeimageurl, ai.biosummary,"
				+ " exists(select 1 from library.artisttoptrackplaycount where artist_id = a.id)"
				+ " from music.artist a"
				+ " left outer join music.artistinfo ai on ai.artist_id = a.id"
				+ " where a.id = ?";

		List<ArtistInfo> artistInfos = jdbcTemplate.query(sql,
				new Object[] { artistId }, new RowMapper<ArtistInfo>() {
					@Override
					public ArtistInfo mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ArtistInfo ai = new ArtistInfo();
						ai.setArtist(new Artist(artistId, rs.getString(1)));
						ai.setLargeImageUrl(rs.getString(2));
						ai.setBioSummary(rs.getString(3));
						ai.setInSearchIndex(rs.getBoolean(4));
						return ai;
					}
				});

		return artistInfos.isEmpty() ? null : artistInfos.get(0);
	}

	@Override
	public ArtistInfo getDetailedArtistInfo(Uri artistUri) {
		final Integer artistId = artistUri.getId();

		String sql = "select a.artist_name_capitalization, ai.largeimageurl, ai.biocontent"
				+ " from music.artist a"
				+ " left outer join music.artistinfo ai on ai.artist_id = a.id"
				+ " where a.id = ?";
		List<ArtistInfo> artistInfos = jdbcTemplate.query(sql,
				new Object[] { artistId }, new RowMapper<ArtistInfo>() {
					@Override
					public ArtistInfo mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ArtistInfo ai = new ArtistInfo();
						ai.setArtist(new Artist(artistId, rs.getString(1)));
						ai.setLargeImageUrl(rs.getString(2));
						ai.setBioContent(rs.getString(3));
						return ai;
					}
				});

		return artistInfos.isEmpty() ? null : artistInfos.get(0);
	}

	@Override
	public ArtistInfo getArtistInfo(final Artist artist) {

		String sql = "select ai.smallimageurl, ai.mediumimageurl, ai.largeimageurl, ai.extralargeimageurl, "
				+ "ai.listeners, ai.playcount, ai.biosummary, ai.biocontent from music.artistinfo ai"
				+ " inner join music.artist a on ai.artist_id = a.id"
				+ " where a.artist_name = upper(?)";

		ArtistInfo artistInfo = jdbcTemplate.queryForObject(sql,
				new Object[] { artist.getName() }, new RowMapper<ArtistInfo>() {
					@Override
					public ArtistInfo mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ArtistInfo ai = new ArtistInfo();
						ai.setArtist(artist);
						ai.setSmallImageUrl(rs.getString(1));
						ai.setMediumImageUrl(rs.getString(2));
						ai.setLargeImageUrl(rs.getString(3));
						ai.setExtraLargeImageUrl(rs.getString(4));
						ai.setListeners(rs.getInt(5));
						ai.setPlayCount(rs.getInt(6));
						ai.setBioSummary(rs.getString(7));
						ai.setBioContent(rs.getString(8));
						return ai;
					}

				});

		return artistInfo;
	}

	@Override
	public void setBioSummary(Uri artistUri, String biography) {
		final Integer artistId = artistUri.getId();
		String sql = "update music.artistinfo set biosummary = ? where artist_id = ?";
		jdbcTemplate.update(sql, biography, artistId);
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