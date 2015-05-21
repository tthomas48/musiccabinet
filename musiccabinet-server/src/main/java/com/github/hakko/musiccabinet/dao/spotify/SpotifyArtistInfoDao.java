package com.github.hakko.musiccabinet.dao.spotify;

import jahspotify.media.Link;
import jahspotify.services.MediaHelper;

import java.util.List;

import com.github.hakko.musiccabinet.configuration.SpotifyUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.dao.ArtistInfoDao;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.spotify.SpotifyService;

public class SpotifyArtistInfoDao implements ArtistInfoDao {

	private static final Logger LOG = Logger
			.getLogger(SpotifyArtistInfoDao.class);

	private SpotifyService spotifyService;

	@Override
	public void createArtistInfo(List<ArtistInfo> artistInfo) {

		// no-op, we can't create data on spotify

	}

	@Override
	public ArtistInfo getArtistInfo(Uri uri) {
		if (uri instanceof SpotifyUri == false) {
			return null;
		}
		Link link = ((SpotifyUri) uri).getLink();
		if (link == null) {
			return null;
		}

		jahspotify.media.Artist artist = spotifyService.readArtist(link);
		ArtistInfo ai = new ArtistInfo();
		ai.setArtist(new Artist(uri, artist.getName()));

		// the JNI code does not seem to populate this yet
		if (artist.getPortraits().size() > 0) {
			ai.setLargeImageUrl("coverArt.view?path="
					+ artist.getPortraits().get(0).asString());
		}
		ai.setBioSummary(artist.getBios());
		// TODO: What does this do?
		ai.setInSearchIndex(false);
		return ai;
	}

	@Override
	public ArtistInfo getDetailedArtistInfo(Uri uri) {
		return getArtistInfo(uri);
	}

	@Override
	public ArtistInfo getArtistInfo(Artist subsonicArtist) {
		Uri uri = subsonicArtist.getUri();
		if (uri instanceof SpotifyUri == false) {
			return null;
		}
		Link link = ((SpotifyUri) uri).getLink();
		if (link == null) {
			return null;
		}
		jahspotify.media.Artist artist = spotifyService.readArtist(link);
		ArtistInfo ai = new ArtistInfo();
		ai.setArtist(subsonicArtist);
		if (!MediaHelper.waitFor(artist, 10)) {
			return ai;
		}

		ai.setSmallImageUrl(artist.getPortraits().get(2).asHTTPLink());
		ai.setMediumImageUrl(artist.getPortraits().get(1).asHTTPLink());
		ai.setLargeImageUrl(artist.getPortraits().get(0).asHTTPLink());
		ai.setExtraLargeImageUrl(artist.getPortraits().get(3).asHTTPLink());
		ai.setListeners(0);
		ai.setPlayCount(0);
		ai.setBioSummary(artist.getBios());
		ai.setBioContent(artist.getBios());

		return ai;
	}

	@Override
	public void setBioSummary(Uri artistUri, String biosummary) {

		// no-op, we can't create data on spotify

	}

	public void setSpotifyService(SpotifyService spotifyService) {
		this.spotifyService = spotifyService;
	}

}
