package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;

public class ArtistRecommendationRowMapper implements RowMapper<ArtistRecommendation> {

	@Override
	public ArtistRecommendation mapRow(ResultSet rs, int rowNum) throws SQLException {
		Uri artistUri = new SubsonicUri(rs.getInt(1));
		String artistName = rs.getString(2);
		String imageUrl = rs.getString(3);
		return new ArtistRecommendation(artistName, imageUrl, artistUri);
	}

}
