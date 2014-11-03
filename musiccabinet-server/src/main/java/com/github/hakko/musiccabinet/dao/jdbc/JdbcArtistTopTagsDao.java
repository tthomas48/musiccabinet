package com.github.hakko.musiccabinet.dao.jdbc;

import static java.lang.String.format;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.ArtistTopTagsDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.TagNameCountRowMapper;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.service.lastfm.LastFmSettingsService;

public class JdbcArtistTopTagsDao implements ArtistTopTagsDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	private LastFmSettingsService settingsService;
        
        private final String GET_MUSIC_FROM_ARTIST_ID="select * from music.get_artist_id(?)";
        
        private final String BATCH_INSERT_ARTIST_TAGS="insert into music.artisttoptag_import (artist_id, tag_name, tag_count) values (?,?,?)";

        private final String GET_TOP_TAGS_URI="select tag_name, tag_count"
			+ " from ? att"
			+ " inner join music.tag tag on att.tag_id = tag.id"
			+ " where att.artist_id = ? order by att.tag_count desc";
        
        private final String GET_TOP_TAGS_URI_LIMIT="select t.tag_name, att.tag_count"
				+ " from ? att"
				+ " inner join music.tag t on att.tag_id = t.id"
				+ " inner join library.toptag tt on tt.tag_id = t.id"
				+ " where att.artist_id = ? and tag_count > 0"
				+ " order by att.tag_count desc limit ?";
        private final String GET_TOP_TAG_ID="select id from music.tag where tag_name = ?";
        
        private final String UPDATE_ARTIST_TOP_TAG="update music.artisttoptag att set tag_count = ?"
				+ " from music.tag t where att.tag_id = t.id"
				+ " and att.artist_id = ? and t.id = ?";
        
        private final String UPDATE_ARTIST_TOP_TAG_2="insert into music.artisttoptag (artist_id, tag_id, tag_count)"
				+ " select %d, %d, %d where not exists (select 1 from music.artisttoptag "
				+ " where artist_id = %d and tag_id = %d)";
        
        
	@Override
	public void createTopTags(Artist artist, List<Tag> tags) {
		if (tags.size() > 0) {
			clearImportTable();
			batchInsert(artist, tags);
			updateTopTags();
		}
	}

	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.artisttoptag_import");
	}

	private void batchInsert(Artist artist, List<Tag> tags) {
		int sourceArtistId = jdbcTemplate.queryForInt(GET_MUSIC_FROM_ARTIST_ID, artist.getName());


		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), BATCH_INSERT_ARTIST_TAGS);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("tag_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("tag_count", Types.SMALLINT));

		for (Tag tag : tags) {
			batchUpdate.update(new Object[]{sourceArtistId, tag.getName(), tag.getCount()});
		}
		batchUpdate.flush();
	}

	private void updateTopTags() {
		jdbcTemplate.execute("select music.update_artisttoptag()");
	}

	@Override
	public List<Tag> getTopTags(Uri artistUri) {
		String topTagsTable = settingsService.getArtistTopTagsTable();

		return jdbcTemplate.query(GET_TOP_TAGS_URI,new Object[]{topTagsTable,artistUri.getId()}, new TagNameCountRowMapper());
	}

	@Override
	public List<Tag> getTopTags(Uri artistUri, int limit) {
		String topTagsTable = settingsService.getArtistTopTagsTable();

		return jdbcTemplate.query(GET_TOP_TAGS_URI_LIMIT,new Object[]{topTagsTable,artistUri.getId(),limit}, new TagNameCountRowMapper());
	}

	@Override
	public void updateTopTag(Uri artistUri, String tagName, int tagCount) {
		
		int artistId = artistUri.getId();
		int tagId = jdbcTemplate.queryForInt(GET_TOP_TAG_ID, tagName);

		jdbcTemplate.update(UPDATE_ARTIST_TOP_TAG,new Object[]{tagCount, artistId, tagId});

		jdbcTemplate.update(UPDATE_ARTIST_TOP_TAG_2,new Object[]{artistId, tagId, tagCount, artistId, tagId});
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	// Spring setters

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void setLastFmSettingsService(LastFmSettingsService settingsService) {
		this.settingsService = settingsService;
	}

}