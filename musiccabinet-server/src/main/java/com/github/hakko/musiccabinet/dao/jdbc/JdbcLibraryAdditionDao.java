package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.Types;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.log.Logger;

public class JdbcLibraryAdditionDao implements LibraryAdditionDao,
		JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	private static final Logger LOG = Logger
			.getLogger(JdbcLibraryAdditionDao.class);

	@Override
	public void clearImport() {
		jdbcTemplate.execute("truncate library.directory_import");
		jdbcTemplate.execute("truncate library.file_import");
		jdbcTemplate.execute("truncate library.file_headertag_import");
	}

	@Override
	public void addSubdirectories(String directory, Set<String> subDirectories) {

		String sql = "insert into library.directory_import (parent_path, path) values (?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(
				jdbcTemplate.getDataSource(), sql);
		batchUpdate.declareParameter(new SqlParameter("parent_path",
				Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("path", Types.VARCHAR));

		for (String subDirectory : subDirectories) {
			batchUpdate.update(new Object[] { directory, subDirectory });
		}
		batchUpdate.flush();

	}

	@Override
	public void addFiles(String directory, Set<File> files) {

		String sql = "insert into library.file_import (path, filename, modified, size) values (?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(
				jdbcTemplate.getDataSource(), sql);
		batchUpdate.declareParameter(new SqlParameter("path", Types.VARCHAR));
		batchUpdate
				.declareParameter(new SqlParameter("filename", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("modified",
				Types.TIMESTAMP));
		batchUpdate.declareParameter(new SqlParameter("size", Types.INTEGER));

		for (File file : files) {
			batchUpdate.update(new Object[] { file.getDirectory(),
					file.getFilename(), file.getModified().toDate(),
					file.getSize() });
		}
		batchUpdate.flush();

		addMetadata(files);
	}

	public void updateMetadata(String directory, String filename, MetaData md) {
		String sql = "update library.file_headertag_set extension = ?,"
				+ "bitrate = ?, vbr = ?, duration = ?, artist_name = ?, album_artist_name = ?, composer_name = ?,"
				+ "album_name = ?, track_name = ?, track_nr = ?, track_nrs = ?, disc_nr = ?, disc_nrs = ?, year = ?,"
				+ "tag_name = ?, lyrics = ?, coverart = ?, artistsort_name = ?, albumartistsort_name = ?, "
				+ "spotify_uri, = ? spotify_artist_uri = ?, spotify_album_uri = ?, explicit = ?) "
				+ "where path = ? and filename = ?";
		if (md != null) {
			String uri = null;
			if (md.getUri() != null) {
				uri = md.getUri().toString();
			}
			String artistUri = null;
			if (md.getArtistUri() != null) {
				artistUri = md.getArtistUri().toString();
			}
			String albumUri = null;
			if (md.getAlbumUri() != null) {
				albumUri = md.getAlbumUri().toString();
			}
			
			String fileSuffix = null;
			if(md.getMediaType() != null) {
				fileSuffix = md.getMediaType().getFilesuffix();
			}

			jdbcTemplate.update(sql, fileSuffix,
					md.getBitrate(), md.isVbr(), md.getDuration(),
					md.getArtist(), md.getAlbumArtist(), md.getComposer(),
					md.getAlbum(), md.getTitle(), md.getTrackNr(),
					md.getTrackNrs(), md.getDiscNr(), md.getDiscNrs(),
					md.getYear(), md.getGenre(), md.getLyrics(),
					md.isCoverArtEmbedded(), md.getArtistSort(),
					md.getAlbumArtistSort(), uri, artistUri, albumUri,
					md.getExplicit(), directory, filename);
		}
	}

	private void addMetadata(Set<File> files) {
		String sql = "insert into library.file_headertag_import (path, filename, extension,"
				+ "bitrate, vbr, duration, artist_name, album_artist_name, composer_name,"
				+ "album_name, track_name, track_nr, track_nrs, disc_nr, disc_nrs, year,"
				+ "tag_name, lyrics, coverart, artistsort_name, albumartistsort_name, spotify_uri, spotify_artist_uri, spotify_album_uri, explicit) values"
				+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		BatchSqlUpdate batch = new BatchSqlUpdate(jdbcTemplate.getDataSource(),
				sql);
		batch.declareParameter(new SqlParameter("path", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("filename", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("extension", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("bitrate", Types.SMALLINT));
		batch.declareParameter(new SqlParameter("vbr", Types.BOOLEAN));
		batch.declareParameter(new SqlParameter("duration", Types.SMALLINT));
		batch.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("album_artist_name",
				Types.VARCHAR));
		batch.declareParameter(new SqlParameter("composer_name", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("album_name", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("track_name", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("track_nr", Types.SMALLINT));
		batch.declareParameter(new SqlParameter("track_nrs", Types.SMALLINT));
		batch.declareParameter(new SqlParameter("disc_nr", Types.SMALLINT));
		batch.declareParameter(new SqlParameter("disc_nrs", Types.SMALLINT));
		batch.declareParameter(new SqlParameter("year", Types.SMALLINT));
		batch.declareParameter(new SqlParameter("tag_name", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("lyrics", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("coverart", Types.BOOLEAN));
		batch.declareParameter(new SqlParameter("artistsort_name",
				Types.VARCHAR));
		batch.declareParameter(new SqlParameter("albumartistsort_name",
				Types.VARCHAR));
		batch.declareParameter(new SqlParameter("spotify_uri", Types.VARCHAR));
		batch.declareParameter(new SqlParameter("spotify_artist_uri",
				Types.VARCHAR));
		batch.declareParameter(new SqlParameter("spotify_album_uri",
				Types.VARCHAR));
		batch.declareParameter(new SqlParameter("explicit", Types.SMALLINT));

		for (File file : files) {
			MetaData md = file.getMetadata();
			if (md != null) {
				String uri = null;
				if (md.getUri() != null) {
					uri = md.getUri().toString();
				}
				String artistUri = null;
				if (md.getArtistUri() != null) {
					artistUri = md.getArtistUri().toString();
				}
				String albumUri = null;
				if (md.getAlbumUri() != null) {
					albumUri = md.getAlbumUri().toString();
				}

				batch.update(new Object[] { file.getDirectory(),
						file.getFilename(), md.getMediaType().getFilesuffix(),
						md.getBitrate(), md.isVbr(), md.getDuration(),
						md.getArtist(), md.getAlbumArtist(), md.getComposer(),
						md.getAlbum(), md.getTitle(), md.getTrackNr(),
						md.getTrackNrs(), md.getDiscNr(), md.getDiscNrs(),
						md.getYear(), md.getGenre(), md.getLyrics(),
						md.isCoverArtEmbedded(), md.getArtistSort(),
						md.getAlbumArtistSort(), uri, artistUri, albumUri,
						md.getExplicit() });
			}
		}
		batch.flush();
	}

	@Override
	public void updateLibrary() {
		long ms = -System.currentTimeMillis();
		jdbcTemplate.execute("select library.add_to_library()");
		ms += System.currentTimeMillis();
		LOG.debug("add_to_library(): " + ms + " ms");
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