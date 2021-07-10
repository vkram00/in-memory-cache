package com.cache.redis.generics;

public class Key<T> {

	private T key;

	public Key(T key) {
		this.key = key;
	}

	public T getKey() {
		return this.key;
	}

	public void setKey(T key) {
		this.key = key;
	}
}
