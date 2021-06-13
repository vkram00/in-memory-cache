package com.cache.redis.models;

import com.cache.redis.interfaces.INode;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class CacheNode implements INode {

	private final String key;
	private String value;
	private final Long ttl;
	private final TimeUnit timeUnit;
	private final Long creationTime;
	private CacheNode nextNode;
	private CacheNode prevNode;

	public CacheNode(String key, String value, Long ttl, TimeUnit tunit) {
		this.key = key;
		this.value = value;
		this.ttl = ttl;
		this.timeUnit = tunit;
		this.creationTime = System.currentTimeMillis();
		this.prevNode = null;
		this.nextNode = null;
	}

	@Override public Long getExpiryTime() {
		return (this.creationTime + this.timeUnit.toMillis(ttl));
	}
}
