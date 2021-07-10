package com.cache.redis.models;

import com.cache.redis.generics.Key;
import com.cache.redis.generics.Value;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter @Setter public class CacheNode {

	private final Key key;
	private Value value;
	private final Long ttl;
	private final TimeUnit timeUnit;
	private final Long creationTime;
	private CacheNode nextNode;
	private CacheNode prevNode;

	public CacheNode(Key key, Value value, Long ttl, TimeUnit tunit) {
		this.key = key;
		this.value = value;
		this.ttl = ttl;
		this.timeUnit = tunit;
		this.creationTime = System.currentTimeMillis();
		this.prevNode = null;
		this.nextNode = null;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public void setPrevNode(CacheNode node) {

		this.prevNode = node;
	}

	public void setNextNode(CacheNode node) {

		this.nextNode = node;
	}

	public Long getExpiryTime() {

		return (this.creationTime + this.timeUnit.toMillis(ttl));
	}
}
