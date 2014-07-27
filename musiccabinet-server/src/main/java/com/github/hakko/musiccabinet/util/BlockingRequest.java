package com.github.hakko.musiccabinet.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.github.hakko.musiccabinet.log.Logger;

public abstract class BlockingRequest<T> {

	private static final Logger LOG = Logger.getLogger(BlockingRequest.class);

	final BlockingQueue<T> blockingQueue = new ArrayBlockingQueue<T>(1);

	public abstract void run();

	public void finish(T result) {
		blockingQueue.add(result);
	}

	public T start() {
		try {
			run();
			return blockingQueue.poll(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.error("Interrupted while waiting on request.", e);
		}
		return null;
	}

}
