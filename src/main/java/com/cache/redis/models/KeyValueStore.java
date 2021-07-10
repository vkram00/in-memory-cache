package com.cache.redis.models;

import com.cache.redis.constants.CacheType;
import com.cache.redis.generics.Key;
import com.cache.redis.generics.Value;
import com.cache.redis.interfaces.IEvictStrategy;
import com.cache.redis.strategy.LFUStrategy;
import com.cache.redis.strategy.LRUStrategy;

import java.util.concurrent.ExecutionException;

public class KeyValueStore {

	private final CacheType cacheType;
	private final IEvictStrategy evictStrategy;
	private int cacheLimit;

	public KeyValueStore(CacheType cacheType, int cacheLimit) {
		this.cacheType = cacheType;
		this.cacheLimit = cacheLimit;
		switch (this.cacheType) {
		case LRU:
			this.evictStrategy = new LRUStrategy(this.cacheLimit);
			break;

		case LFU:
			this.evictStrategy = new LFUStrategy(this.cacheLimit);
			break;
		default:
			throw new IllegalStateException("no such cache type exist!!!");
		}
	}

	public Value get(Key key) throws ExecutionException, InterruptedException {
		return this.evictStrategy.get(key);
	}

	public void put(Key key, Value value, Long ttl) {
		this.evictStrategy.put(key, value, ttl);
	}

}
