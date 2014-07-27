package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import static java.io.File.separatorChar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.configuration.SpotifyUri;
import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.music.Album;

public class AlbumRowMapper implements RowMapper<Album> {

	@Override
	public Album mapRow(ResultSet rs, int rowNum) throws SQLException {
		int artistId = rs.getInt(1);
		String artistName = rs.getString(2);
		int albumId = rs.getInt(3);
		String albumName = rs.getString(4);
		int year = rs.getInt(5);
		String coverArtFile = getFileName(rs.getString(6), rs.getString(7));
		boolean coverArtEmbedded = coverArtFile != null;
		if (!coverArtEmbedded) {
			coverArtFile = getFileName(rs.getString(8), rs.getString(9));
		}
		String coverArtURL = rs.getString(10);
		Integer[] trackIds = (Integer[])rs.getArray(11).getArray();
		
		Uri spotifyUri = null;
		String spotifyUriString = rs.getString(12);
		if(StringUtils.isNotEmpty(spotifyUriString)) {
		  spotifyUri = new SpotifyUri(spotifyUriString);	
		}
		List<Uri> trackUris = new ArrayList<Uri>();
		for(Integer trackId : trackIds) {
			trackUris.add(new SubsonicUri(trackId));
		}
		return new Album(artistId, artistName, albumId, albumName, year, coverArtFile,
				coverArtEmbedded, coverArtURL, trackUris, spotifyUri);
	}

	private String getFileName(String directory, String filename) {
		return directory == null || filename == null ? null : 
			directory + separatorChar + filename;
	}

}
