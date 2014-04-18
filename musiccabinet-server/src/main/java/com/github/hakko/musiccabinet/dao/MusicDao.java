package com.github.hakko.musiccabinet.dao;

import java.util.List;
import java.util.Set;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface MusicDao {

	Uri getArtistUri(String artistName);
	Uri getArtistUri(Artist artist);
	List<Artist> getArtists(Set<Uri> artistUris);
	Uri getAlbumUri(String artistName, String albumName);
	Uri getAlbumUri(Album album);
	Uri getTrackUri(String artistName, String trackName);
	Uri getTrackUri(Track track);
	Track getTrack(Uri trackUri);

}