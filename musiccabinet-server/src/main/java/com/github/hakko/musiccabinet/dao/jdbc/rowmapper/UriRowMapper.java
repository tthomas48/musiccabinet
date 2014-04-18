package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.configuration.Uri;

public class UriRowMapper implements RowMapper<Uri> {

	@Override
	public Uri mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new SubsonicUri(rs.getInt(1));
	}
}