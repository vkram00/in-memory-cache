package com.cache.redis.interfaces;

import com.cache.redis.generics.Key;
import com.cache.redis.generics.Value;

import java.util.concurrent.ExecutionException;

public interface IEvictStrategy {

	public Value get(Key key) throws ExecutionException, InterruptedException;

	public void put(Key key, Value value, Long ttl);

	public Runnable cleanUp();

}
