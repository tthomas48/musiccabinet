package com.github.hakko.musiccabinet.dao.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.ArtistRowMapper;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.TrackWithArtistRowMapper;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcMusicDao implements MusicDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	public void setArtistUri(Artist artist) {
		artist.setUri(getArtistUri(artist.getName()));
	}
	
	public Uri getArtistUri(String artistName) {
		return new SubsonicUri(jdbcTemplate.queryForInt(
			"select * from music.get_artist_id(?)", artistName));
	}
	
	public Uri getArtistUri(Artist artist) {
		return getArtistUri(artist.getName());
	}
	
	public Artist getArtist(String artistName) {
		String sql = "select artist_name_capitalization from music.artist where artist_name = upper(?)";
		return new Artist(jdbcTemplate.queryForObject(sql, String.class, artistName));
	}

	public List<Artist> getArtists(Set<Uri> artistIds) {
		if (artistIds.isEmpty()) {
			return new ArrayList<>();
		}
		
		
		String sql = "select id, artist_name_capitalization from music.artist"
				+ " where id in (" + PostgreSQLUtil.getUriParameters(artistIds) + ")"
				+ " order by artist_name_capitalization";
		
		return jdbcTemplate.query(sql, new ArtistRowMapper());
	}
	
	public Uri getAlbumUri(String artistName, String albumName) {
		return new SubsonicUri(jdbcTemplate.queryForInt(
				"select * from music.get_album_id(?,?)",
				artistName, albumName));
	}
	
	public Uri getAlbumUri(Album album) {
		return getAlbumUri(album.getArtist().getName(), album.getName());
	}
	
	public Uri getTrackUri(String artistName, String trackName) {
		return new SubsonicUri(jdbcTemplate.queryForInt(
			"select * from music.get_track_id(?,?)", 
			artistName, trackName));
	}
	
	public Uri getTrackUri(Track track) {
		return getTrackUri(track.getArtist().getName(), track.getName());
	}

	@Override
	public Track getTrack(Uri trackUri) {
		Integer trackId = trackUri.getId();
		
		String sql = "select a.artist_name_capitalization, t.track_name_capitalization"
				+ " from music.artist a inner join music.track t on t.artist_id = a.id"
				+ " where t.id = " + trackId;

		return jdbcTemplate.queryForObject(sql, new TrackWithArtistRowMapper());
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