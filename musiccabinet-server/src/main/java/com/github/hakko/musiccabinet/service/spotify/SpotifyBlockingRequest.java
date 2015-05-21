package com.github.hakko.musiccabinet.service.spotify;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.github.hakko.musiccabinet.util.BlockingRequest;

public abstract class SpotifyBlockingRequest<T> extends BlockingRequest<T> {

	private ReentrantLock spotifyLock = new ReentrantLock(true);

	private T defaultValue;

	public SpotifyBlockingRequest() {
		this(null);
	}

	public SpotifyBlockingRequest(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	protected boolean lock() {
		try {
			return spotifyLock.tryLock(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	protected void unlock() {
		if (!spotifyLock.isHeldByCurrentThread() || !spotifyLock.isLocked()) {
			throw new RuntimeException("Tried to unlock a lock I didn't hold");
		}
		spotifyLock.unlock();
	}

	@Override
	public T start() {
		try {
			if (!lock()) {
				return defaultValue;
			}
			return super.start();
		} finally {
			unlock();
		}
	}

}
