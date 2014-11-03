package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil.getParameters;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.TagDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.TagIdNameRowMapper;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.TagOccurrenceRowMapper;
import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;
import com.github.hakko.musiccabinet.domain.model.aggr.TagTopArtists;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.service.lastfm.LastFmSettingsService;

public class JdbcTagDao implements TagDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	private LastFmSettingsService settingsService;
        
        private final String CREATE_TAGS="insert into music.tag (tag_name) select distinct (?)"
				+ " where not exists (select 1 from music.tag where tag_name = ?)";
        
        private final String GET_AVALABLE_TAGS="select * from"
				+ " (select t.tag_name, tc.tag_name, occ.count,"
				+ " case when tt.tag_id is null then false else true end from music.tag t"
				+ " inner join (select tag_id, count(tag_id) from ? att"
				+ " where tag_count > 25 group by tag_id) occ on occ.tag_id = t.id"
				+ " left outer join music.tag tc on t.corrected_id = tc.id"
				+ " left outer join (select tag_id from library.toptag) tt on t.id = tt.tag_id"
				+ " where occ.count >= 5"
				+ " order by occ.count desc limit 250) top order by 1";
        
        private final String GET_TOP_TAGS_OCCURENCE="select tag.tag_name, null, 10+ntile(30) over (order by pop.sum), true from library.toptag tt"
				+ " inner join music.tag tag on tt.tag_id = tag.id"
				+ " inner join (select coalesce(t.corrected_id, t.id) as tag_id, sum(tag_count)"
				+ "  from ? att inner join music.tag t on att.tag_id = t.id"
				+ "  group by coalesce(t.corrected_id, t.id)) pop on tag.id = pop.tag_id"
				+ " order by tag.tag_name";
        
        private final String GET_TAGS_WITHOUT_TOP_ARTIST="select t.id, t.tag_name from music.tag t"
				+ " left outer join (select tag_id, count(tag_id) from ? where tag_count > 25 group by tag_id) occ on t.id = occ.tag_id"
				+ " left outer join (select tag_id, sum(tag_count) from ? group by tag_id) pop on t.id = pop.tag_id"
				+ " left outer join (select tag_id from library.toptag) tt on t.id = tt.tag_id"
				+ " where ((occ.count > 5 and pop.sum/occ.count > 50)"
				+ " or t.id in (select tag_id from library.toptag))"
				+ " and not exists (select 1 from music.tagtopartist where tag_id = t.id)";
        
        private final String BATCH_INSERT_TOP_TAG="insert into music.tagtopartist_import (tag_name, artist_name, rank) values (?,?,?)";

	@Override
	public void createTags(List<String> tags) {

		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), CREATE_TAGS);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("tag_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("tag_name", Types.VARCHAR));

		for (String tag : tags) {
			batchUpdate.update(new Object[]{tag, tag});
		}
		batchUpdate.flush();
	}

	@Override
	public List<Tag> getTags() {
		String sql = "select id, tag_name from music.tag";

		return jdbcTemplate.query(sql, new TagIdNameRowMapper());
	}

	/*
	 * tagCorrection is a map of <original tag name> -> <corrected tag name>
	 */
	@Override
	public void createTagCorrections(Map<String, String> tagCorrections) {
		String sql = "update music.tag t set corrected_id = null where corrected_id is not null";
		jdbcTemplate.execute(sql);

		if (tagCorrections.size() == 0) return;

		sql = getCreateMissingTagsSql(tagCorrections.size());
		jdbcTemplate.update(sql, (Object[]) tagCorrections.keySet().
				toArray(new String[tagCorrections.size()]));
		jdbcTemplate.update(sql, (Object[]) tagCorrections.values().
				toArray(new String[tagCorrections.size()]));

		sql = "update music.tag t set corrected_id = tc.id"
				+ " from music.tag tc where t.tag_name = ? and tc.tag_name = ?";

		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("t.tag_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("tc.tag_name", Types.VARCHAR));

		for (String tag : tagCorrections.keySet()) {
			batchUpdate.update(new Object[]{tag, tagCorrections.get(tag)});
		}
		batchUpdate.flush();
	}

	protected String getCreateMissingTagsSql(int tags) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into music.tag (tag_name) select distinct tag_name from (values (?)");
		for (int i = 1; i < tags; i++) {
			sb.append(",(?)");
		}
		sb.append(") t (tag_name) where not exists ");
		sb.append("(select 1 from music.tag where tag_name = t.tag_name)");
		return sb.toString();
	}

	@Override
	public Map<String, String> getCorrectedTags() {
		String sql = "select t.tag_name, tc.tag_name from music.tag t"
				+ " inner join music.tag tc on t.corrected_id = tc.id";

		final Map<String, String> correctedTags = new HashMap<>();
		jdbcTemplate.query(sql, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				correctedTags.put(rs.getString(1), rs.getString(2));
			}
		});
		return correctedTags;
	}

	/*
	 * Returns a list of fairly popular and relevant tags that are used for artists
	 * found in library.
	 *
	 * The tags chosen are the top 250 ordered by having most unique artists with a
	 * tag count of > 25, and a minimum of 5 artists being tagged with it.
	 */
	@Override
	public List<TagOccurrence> getAvailableTags() {
		String topTagsTable = settingsService.getArtistTopTagsTable();

		return jdbcTemplate.query(GET_AVALABLE_TAGS,new Object[]{topTagsTable}, new TagOccurrenceRowMapper());
	}

	@Override
	public void setTopTags(final List<String> topTags) {
		assert(topTags != null && topTags.size() > 0);

		jdbcTemplate.update("truncate library.toptag");

		String sql = "insert into library.toptag (tag_id)"
			+ " select id from music.tag where tag_name in ("
			+ getParameters(topTags.size()) + ")";

		jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int index = 1;
				for (String topTag : topTags) {
					ps.setString(index++, topTag);
				}
			}
		});
	}

	@Override
	public List<String> getTopTags() {
		String sql =
			"select tag.tag_name from library.toptag tt"
			+ " inner join music.tag tag on tt.tag_id = tag.id"
			+ " order by lower(tag.tag_name)";

		return jdbcTemplate.queryForList(sql, String.class);
	}

	/*
	 * Returns top tags (prerequisite), together with an equally distributed weighted
	 * system indicating popularity for each tag.
	 *
	 * Right now, the popularity comes distributed in the interval 10-40, to support a
	 * tag cloud using the popularity as font size.
	 *
	 * TODO : turn min & max values into parameters
	 */
	@Override
	public List<TagOccurrence> getTopTagsOccurrence() {
		String topTagsTable = settingsService.getArtistTopTagsTable();
                
		return jdbcTemplate.query(GET_TOP_TAGS_OCCURENCE, new Object[]{topTagsTable},new TagOccurrenceRowMapper());
	}

	@Override
	public List<Tag> getTagsWithoutTopArtists() {
		String topTagsTable = settingsService.getArtistTopTagsTable();

		return jdbcTemplate.query(GET_TAGS_WITHOUT_TOP_ARTIST,new Object[]{topTagsTable,topTagsTable}, new TagIdNameRowMapper());
	}

	@Override
	public void createTopArtists(List<TagTopArtists> tagTopArtists) {
		if (tagTopArtists.size() > 0) {
			clearImportTable();
			for (TagTopArtists tta : tagTopArtists) {
				batchInsert(tta.getTagName(), tta.getArtists());
			}
			updateUserTopArtists();
		}
	}

	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.tagtopartist_import");
	}

	private void batchInsert(String tagName, List<Artist> artists) {

		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), BATCH_INSERT_TOP_TAG);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("tag_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("rank", Types.INTEGER));

		for (int i = 0; i < artists.size(); i++) {
			batchUpdate.update(new Object[]{tagName, artists.get(i).getName(), i});
		}
		batchUpdate.flush();
	}

	private void updateUserTopArtists() {
		jdbcTemplate.execute("select music.update_tagtopartists()");
	}

	@Override
	public List<String> getFileTags() {
		return jdbcTemplate.queryForList("select t.tag_name from music.tag t"
				+ " where id in (select tag_id from library.filetag)"
				+ " order by tag_name", String.class);
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