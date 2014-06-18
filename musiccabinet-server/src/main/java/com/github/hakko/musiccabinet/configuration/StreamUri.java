package com.github.hakko.musiccabinet.configuration;

import java.net.URI;
import java.net.URISyntaxException;

public class StreamUri implements Uri {

	private URI uri;
	
	public StreamUri(URI uri) {
		this.uri = uri;
	}

	public StreamUri(String uri) {
		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int compareTo(Uri o) {
		return o.getId().compareTo(-1);
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public Integer getId() {
		return -1;
	}
	
	@Override
	public String toString() {
		return uri.toString();
	}

}
