package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;

public class Id<T> {

	private T value;
	public Id(T value) {
		
		this.value = value;
	}
	
	public T getValue() {
		return this.value;
	}
	
	@Override
	public String toString() {
		return this.value.toString();
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  Id t = (Id) o;
          return new EqualsBuilder()
          .append(value, t.value)
          .isEquals();
	}

}
