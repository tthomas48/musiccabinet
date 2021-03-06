package com.github.hakko.musiccabinet.configuration;

import jahspotify.media.Link;

import java.net.URI;

import org.apache.http.client.utils.URIUtils;

import com.github.hakko.musiccabinet.dao.util.URIUtil;

public class SpotifyUri implements Uri {
	
	private static final long serialVersionUID = 7021929381296436638L;
	
	private URI uri;
	
	public SpotifyUri(String uri) {
		this.uri = URIUtil.getURI(Link.create(uri));
	}
	
	public SpotifyUri(Link link) {
		this.uri = URIUtil.getURI(link);
		
	}
	
	public SpotifyUri(URI uri) {
		this.uri = uri;
	}

	@Override
	public URI getUri() {
		return uri;
	}
	
	@Override
	public String toString() {
		return uri.toString();
	}
	
	
	
	@Override
	public int hashCode() {
		return uri.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || obj instanceof SpotifyUri == false) {
			return false;
		}

		return uri.equals(((SpotifyUri) obj).getUri());
	}
	
	@Override
	public Integer getId() {
		return -1;
	}
	
	public Link getLink() {
		return URIUtil.getSpotifyLink(uri);
	}
	
	@Override
	public int compareTo(Uri o) {
		return o.getId().compareTo(-1);
	}
	
}
