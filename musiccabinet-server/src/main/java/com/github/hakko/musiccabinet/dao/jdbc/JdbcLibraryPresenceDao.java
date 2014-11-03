package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.github.hakko.musiccabinet.dao.LibraryPresenceDao;
import com.github.hakko.musiccabinet.domain.model.library.File;

public class JdbcLibraryPresenceDao implements LibraryPresenceDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
        
        private final String EXISTS="select exists(select 1 from library.directory where path = ?)";
        
        private final String GET_SUB_DIRECTORIES="select d2.path from library.directory d2"
				+ " inner join library.directory d1"
				+ " on d2.parent_id = d1.id and d1.path = ?";
        
        private final String GET_FILES="select d.path, f.filename, f.modified, f.size from library.file f"
				+ " inner join library.directory d on f.directory_id = d.id"
				+ " where d.path = ?";

	@Override
	public boolean exists(String directory) {
		return jdbcTemplate.queryForObject(EXISTS, Boolean.class, directory);
	}

	@Override
	public Set<String> getSubdirectories(String directory) {
		String sql = "select d2.path from library.directory d2"
				+ " inner join library.directory d1"
				+ " on d2.parent_id = d1.id and d1.path = ?";

		return new HashSet<>(jdbcTemplate.queryForList(GET_SUB_DIRECTORIES, String.class, directory));
	}

	@Override
	public Set<File> getFiles(String directory) {

		final Set<File> files = new HashSet<>();
		jdbcTemplate.query(GET_FILES, new Object[]{directory}, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				String directory = rs.getString(1);
				String filename = rs.getString(2);
				DateTime modified = new DateTime(rs.getTimestamp(3).getTime());
				int size = rs.getInt(4);
				files.add(new File(directory, filename, modified, size));
			}
		});
		
		return files;
	}
	
	@Override
	public List<String> getRootDirectories() {
		String sql = "select path from library.directory where parent_id is null";
		
		return jdbcTemplate.queryForList(sql, String.class);
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