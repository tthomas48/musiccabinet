package com.github.hakko.musiccabinet.configuration;

import java.net.URI;

import com.github.hakko.musiccabinet.dao.util.URIUtil;

public class SubsonicUri implements Uri {
	
	private static final long serialVersionUID = 6942450687801550196L;
	
	private URI uri;
	
	public SubsonicUri(URI uri) {
		this.uri = uri;
	}
	
	public SubsonicUri(Integer id) {
		this.uri = URIUtil.getSubsonicUri(id);
	}
	
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
		if(obj == null || obj instanceof SubsonicUri == false) {
			return false;
		}

		return uri.equals(((SubsonicUri) obj).getUri());
	}
	
	@Override
	public Integer getId() {
		return URIUtil.getSubsonicId(uri);
	}

	@Override
	public int compareTo(Uri arg0) {
		return arg0.getId().compareTo(arg0.getId());
	}	
}
