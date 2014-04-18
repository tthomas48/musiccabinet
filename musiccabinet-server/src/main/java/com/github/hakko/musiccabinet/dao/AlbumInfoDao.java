package com.github.hakko.musiccabinet.dao;

import java.util.List;
import java.util.Map;

import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.AlbumInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public interface AlbumInfoDao {

	void createAlbumInfo(List<AlbumInfo> albumInfos);
	AlbumInfo getAlbumInfo(Uri albumUri) throws ApplicationException;
	AlbumInfo getAlbumInfo(Album album) throws ApplicationException;
	List<Album> getAlbumsWithoutInfo();
	List<AlbumInfo> getAlbumInfosForArtist(Uri artistUri);
	Map<Uri, AlbumInfo> getAlbumInfosForUris(List<Uri> albumUris);
	
}
