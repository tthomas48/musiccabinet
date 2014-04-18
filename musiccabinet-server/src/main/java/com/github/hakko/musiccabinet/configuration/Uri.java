package com.github.hakko.musiccabinet.configuration;

import java.net.URI;

public interface Uri extends Comparable<Uri> {

	public URI getUri();
	
	public Integer getId();	
}
