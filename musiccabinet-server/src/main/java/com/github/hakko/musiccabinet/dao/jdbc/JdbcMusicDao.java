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
        
        private final String GET_ARTIST_URI="select * from music.get_artist_id(?)";
        
        private final String GET_ARTIST="select artist_name_capitalization from music.artist where artist_name = upper(?)";
        
        private final String GET_ARTISTS_LIST="select id, artist_name_capitalization from music.artist"
				+ " where id in ( ? )"
				+ " order by artist_name_capitalization";
        
        private final String GET_ALBUM_URI="select * from music.get_album_id(?,?)";
        
        private final String GET_TRACK_URI="select * from music.get_track_id(?,?)";
        
        private final String GET_TRACK="select a.artist_name_capitalization, t.track_name_capitalization"
				+ " from music.artist a inner join music.track t on t.artist_id = a.id"
				+ " where t.id = ?";
	
	public void setArtistUri(Artist artist) {
		artist.setUri(getArtistUri(artist.getName()));
	}
	
	public Uri getArtistUri(String artistName) {
		return new SubsonicUri(jdbcTemplate.queryForInt(GET_ARTIST_URI,new Object[]{artistName} ));
	}
	
	public Uri getArtistUri(Artist artist) {
		return getArtistUri(artist.getName());
	}
	
	public Artist getArtist(String artistName) {
		return new Artist(jdbcTemplate.queryForObject(GET_ARTIST, String.class, artistName));
	}

	public List<Artist> getArtists(Set<Uri> artistIds) {
		if (artistIds.isEmpty()) {
			return new ArrayList<>();
		}

		return jdbcTemplate.query(GET_ARTISTS_LIST,new Object[]{PostgreSQLUtil.getUriParameters(artistIds)}, new ArtistRowMapper());
	}
	
	public Uri getAlbumUri(String artistName, String albumName) {
		return new SubsonicUri(jdbcTemplate.queryForInt(
				GET_ALBUM_URI,new Object[]{artistName, albumName}));
	}
	
	public Uri getAlbumUri(Album album) {
		return getAlbumUri(album.getArtist().getName(), album.getName());
	}
	
	public Uri getTrackUri(String artistName, String trackName) {
		return new SubsonicUri(jdbcTemplate.queryForInt(
			GET_TRACK_URI, new Object[]{artistName, trackName}));
	}
	
	public Uri getTrackUri(Track track) {
		return getTrackUri(track.getArtist().getName(), track.getName());
	}

	@Override
	public Track getTrack(Uri trackUri) {
		Integer trackId = trackUri.getId();

		return jdbcTemplate.queryForObject(GET_TRACK,new Object[]{trackId}, new TrackWithArtistRowMapper());
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