package com.cache.redis.interfaces;

import java.util.NoSuchElementException;

public interface IKeyValueStore {

	public Runnable cleanUp();
}
