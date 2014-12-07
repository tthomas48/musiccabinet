package com.github.hakko.musiccabinet.configuration;

import java.io.Serializable;
import java.net.URI;

public interface Uri extends Comparable<Uri>, Serializable {

	public URI getUri();
	
	public Integer getId();
	
}
