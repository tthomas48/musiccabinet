package com.github.hakko.musiccabinet.dao.aggregate;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.ArtistInfoDao;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;

public class AggregateArtistInfoDao implements ArtistInfoDao {

	private List<ArtistInfoDao> daos;

	@Override
	public void createArtistInfo(List<ArtistInfo> artistInfo) {
		for (ArtistInfoDao dao : daos) {
			dao.createArtistInfo(artistInfo);
		}
	}

	@Override
	public ArtistInfo getArtistInfo(Artist artist) {
		ArtistInfo artistInfo = null;
		for (ArtistInfoDao dao : daos) {
			artistInfo = dao.getArtistInfo(artist);
			if (artistInfo != null) {
				return artistInfo;
			}
		}
		return null;
	}

	@Override
	public ArtistInfo getArtistInfo(Uri artistUri) {
		
		if(artistUri == null) {
			return null;
		}
			
		ArtistInfo artistInfo = null;
		for (ArtistInfoDao dao : daos) {
			artistInfo = dao.getArtistInfo(artistUri);
			if (artistInfo != null) {
				return artistInfo;
			}
		}
		return null;
	}

	@Override
	public ArtistInfo getDetailedArtistInfo(Uri artistUri) {
		ArtistInfo artistInfo = null;
		for (ArtistInfoDao dao : daos) {
			artistInfo = dao.getArtistInfo(artistUri);
			if (artistInfo != null) {
				return artistInfo;
			}
		}
		return null;
	}

	@Override
	public void setBioSummary(Uri artistUri, String biosummary) {
		for (ArtistInfoDao dao : daos) {
			dao.setBioSummary(artistUri, biosummary);
		}
	}

	public void setDaos(List<ArtistInfoDao> daos) {
		this.daos = daos;
	}

}
