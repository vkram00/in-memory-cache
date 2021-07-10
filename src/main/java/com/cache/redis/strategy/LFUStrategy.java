package com.cache.redis.strategy;

import com.cache.redis.generics.Key;
import com.cache.redis.generics.Value;
import com.cache.redis.interfaces.IEvictStrategy;

public class LFUStrategy implements IEvictStrategy {

	private final int cacheLimit;

	public LFUStrategy(int cacheLimit) {
		this.cacheLimit = cacheLimit;
	}

	@Override public Value get(Key key) {
		return null;
	}

	@Override public void put(Key key, Value value, Long ttl) {

	}

	@Override public Runnable cleanUp() {
		return null;
	}
}
