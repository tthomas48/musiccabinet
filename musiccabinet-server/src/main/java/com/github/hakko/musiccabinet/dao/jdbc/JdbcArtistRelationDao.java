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

import com.github.hakko.musiccabinet.dao.ArtistRelationDao;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistRelation;
import com.github.hakko.musiccabinet.service.lastfm.LastFmSettingsService;

public class JdbcArtistRelationDao implements ArtistRelationDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	private LastFmSettingsService settingsService;
        
        private final String BATCH_INSERT_ARTIST_RELATION="insert into music.artistrelation_import (source_id, target_artist_name, weight) values (?,?,?)";

        private final String GET_ARTIST_RELATIONS="select artist_name_capitalization, weight"
			+ " from music.artistrelation"
			+ " inner join music.artist on music.artistrelation.target_id = music.artist.id"
			+ " where music.artistrelation.source_id = ?";
        
        private final String TOP_TAGS="select tag_id, tag_count from ? where artist_id = ? order by tag_count desc limit 5";
        
        private final String INSERT_SIMILARITY="insert into similarity (artist_id, count)"
				+ " select artist_id, 100-abs(?-tag_count) from"
				+ " ? where tag_id = ?";
	@Override
	public void createArtistRelations(Artist sourceArtist, List<ArtistRelation> artistRelations) {
		if (artistRelations.size() > 0) {
			clearImportTable();
			batchInsert(sourceArtist, artistRelations);
			updateLibrary();
		}
	}

	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.artistrelation_import");
	}

	private void batchInsert(Artist sourceArtist, List<ArtistRelation> ArtistRelations) {
		int sourceArtistId = jdbcTemplate.queryForInt("select * from music.get_artist_id(?)",
				sourceArtist.getName());

		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), BATCH_INSERT_ARTIST_RELATION);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("source_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("target_artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("weight", Types.FLOAT));

		for (ArtistRelation ar : ArtistRelations) {
			batchUpdate.update(new Object[]{
					sourceArtistId, ar.getTarget().getName(), ar.getMatch()});
		}
		batchUpdate.flush();
	}

	private void updateLibrary() {
		jdbcTemplate.execute("select music.update_artistrelation()");
	}

	@Override
	public List<ArtistRelation> getArtistRelations(Artist sourceArtist) {
		final int sourceArtistId = jdbcTemplate.queryForInt(
				"select * from music.get_artist_id(?)", sourceArtist.getName());


		List<ArtistRelation> artistRelations = jdbcTemplate.query(GET_ARTIST_RELATIONS,
				new Object[]{sourceArtistId}, new RowMapper<ArtistRelation>() {
			@Override
			public ArtistRelation mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				String artistName = rs.getString(1);
				float match = rs.getFloat(2);
				return new ArtistRelation(new Artist(artistName), match);
			}
		});

		return artistRelations;
	}

	/*
	 * TODO
	 *
	 * on "Related artists" page, add an "Edit" possibility
	 * Display top tags for main artist
	 * Allow user to select which ones to use
	 * Return matching artists (see below)
	 * Add selected matching artists as related
	 *
	 * somehow mark as updated to avoid overwriting
	 */
	public List<ArtistRelation> getSimilarArtistsByTags(int artistId) {
		String topTagsTable = settingsService.getArtistTopTagsTable();

		String similarityTableSql =
				"create temporary table similarity"
				+ " (artist_id integer not null, count integer not null";

		String topArtistsSql =
				"select a.artist_name_capitalization, m.sum/(100.0*?) from music.artist a"
				+ " inner join (select artist_id, sum(count) from similarity group by artist_id) s"
				+ " on a.id = s.artist_id order by m.sum desc";

		List<TagCount> topTags = jdbcTemplate.query(TOP_TAGS,new Object[]{topTagsTable,artistId}, new RowMapper<TagCount>() {
			@Override
			public TagCount mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				return new TagCount(rs.getInt(1), rs.getInt(2));
			}
		});

		jdbcTemplate.execute(similarityTableSql);
		for (TagCount topTag : topTags) {
			jdbcTemplate.update(INSERT_SIMILARITY,new Object[]{topTag.tagCount, topTag.tagId});
		}

		return jdbcTemplate.query(topArtistsSql, new Object[]{topTags.size()},
				new RowMapper<ArtistRelation>() {
			@Override
			public ArtistRelation mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new ArtistRelation(new Artist(rs.getString(1)), rs.getFloat(2));
			}
		});
	}


	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	private class TagCount {
		protected int tagId;
		protected int tagCount;

		public TagCount(int id, int count) {
			this.tagId = id;
			this.tagCount = count;
		}
	}

	// Spring setters

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setLastFmSettingsService(LastFmSettingsService settingsService) {
		this.settingsService = settingsService;
	}

}