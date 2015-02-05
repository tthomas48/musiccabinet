package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.dao.util.URIUtil;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class ArtistRowMapper implements RowMapper<Artist> {

	@Override
	public Artist mapRow(ResultSet rs, int rowNum) throws SQLException {
		Artist artist = new Artist(rs.getInt(1), rs.getString(2));
		artist.setSpotifyUri(URIUtil.parseURI(rs.getString(3)));
		return artist;
	}

}
