package com.github.hakko.musiccabinet.dao.util;

import jahspotify.media.Link;
import jahspotify.media.Link.InvalidSpotifyURIException;

import java.net.URI;
import java.net.URISyntaxException;

import com.github.hakko.musiccabinet.configuration.SpotifyUri;
import com.github.hakko.musiccabinet.configuration.StreamUri;
import com.github.hakko.musiccabinet.configuration.SubsonicUri;
import com.github.hakko.musiccabinet.configuration.Uri;
import com.github.hakko.musiccabinet.log.Logger;

public class URIUtil {
	public static final String SUBSONIC_PREFIX = "subsonic:";
	public static final String SPOTIFY_PREFIX = "spotify:";
	public static final String STREAM_PREFIX = "http:";
	private static final Logger LOG = Logger.getLogger(URIUtil.class);

	public static Uri parseURI(String uri) {
		try {
			if (uri.startsWith(SUBSONIC_PREFIX)) {
				return new SubsonicUri(new URI(uri));
			} else if (uri.startsWith(SPOTIFY_PREFIX)) {
				return new SpotifyUri(new URI(uri));
			} else if (uri.startsWith(STREAM_PREFIX)) {
				return new StreamUri(new URI(uri));
			}
		} catch (Exception e) {
			LOG.error("Unable to parse uri " + uri, e);
		}
		return null;
	}
	
	public static URI getSubsonicUri(int id) {
		try {
			return new URI(SUBSONIC_PREFIX + id);
		} catch (URISyntaxException e) {
			LOG.error("Unable to parse subsonic:" + id, e);

		}
		return null;
	}

	public static URI getURI(Link link) {
		try {
			if(link.getUri() != null) {
			  return new URI(link.getUri());
			}
			return new URI(link.getId());
		} catch (URISyntaxException e) {
			LOG.error("Unable to parse " + link.toString(), e);

		}
		return null;
	}

	public static Link getSpotifyLink(URI uri) {
		if (!isSpotify(uri)) {
			return null;
		}
		return getSpotifyLink(uri.toString());
	}

	public static Link getSpotifyLink(String uri) {
		if (!isSpotify(uri)) {
			return null;
		}

		try {
			return Link.create(uri);
		} catch (InvalidSpotifyURIException e) {
			LOG.error("Unable to parse " + uri, e);
		}
		return null;
	}

	public static boolean isSpotify(URI uri) {
		if (uri == null) {
			return false;
		}
		return isSpotify(uri.toString());
	}
	
	public static boolean isRemote(Uri uri) {
		return uri instanceof SpotifyUri || uri instanceof StreamUri;
	}
	
	public static boolean isSpotify(Uri uri) {
		return uri instanceof SpotifyUri;
	}
	
	
	public static boolean isSubsonic(Uri uri) {
		return uri instanceof SubsonicUri;
	}

	public static boolean isSpotify(String uri) {
		if (uri == null) {
			return false;
		}
		if (!uri.startsWith(SPOTIFY_PREFIX)) {
			return false;
		}
		return true;
	}

	public static Integer getSubsonicId(String artistString) {
		if (artistString == null) {
			return null;
		}
		if (artistString.startsWith(SUBSONIC_PREFIX)) {
			return Integer.parseInt(artistString.substring(SUBSONIC_PREFIX
					.length()));
		}
		return null;

	}

	public static Integer getSubsonicId(URI artistUri) {
		if (artistUri == null) {
			return null;
		}
		String artistString = artistUri.toString();
		return getSubsonicId(artistString);
	}

}
