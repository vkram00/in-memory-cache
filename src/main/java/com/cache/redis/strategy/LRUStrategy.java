package com.cache.redis.strategy;

import com.cache.redis.generics.Key;
import com.cache.redis.generics.Value;
import com.cache.redis.interfaces.IEvictStrategy;
import com.cache.redis.models.CacheNode;

import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class LRUStrategy implements IEvictStrategy {

	private final static TimeUnit nodeTimeUnit = TimeUnit.SECONDS;
	private final int cacheLimit;
	private AtomicInteger currSize;
	private CacheNode headNode;
	private CacheNode tailNode;
	private final ConcurrentHashMap<Key, CacheNode> nodeMap;
	private final ReentrantLock lock;
	private final ExecutorService executorService;
	private final ScheduledExecutorService cleanUpService;

	public LRUStrategy(int cacheLimit) {
		this.headNode = new CacheNode(new Key("cacheHeadNode"), new Value("CacheHeadValue"), Long.MAX_VALUE,
				nodeTimeUnit);
		this.tailNode = new CacheNode(new Key("cacheTailNode"), new Value("CacheTailValue"), Long.MAX_VALUE,
				nodeTimeUnit);
		this.headNode.setNextNode(this.tailNode);
		this.tailNode.setPrevNode(this.headNode);

		this.nodeMap = new ConcurrentHashMap<>();
		this.lock = new ReentrantLock();
		this.cacheLimit = cacheLimit;
		this.currSize = new AtomicInteger(0);
		executorService = Executors.newSingleThreadExecutor();
		cleanUpService = Executors.newSingleThreadScheduledExecutor();
		cleanUpService.scheduleAtFixedRate(cleanUp(), 1, nodeTimeUnit.toSeconds(1L), nodeTimeUnit);
	}

	private synchronized void reOrderQueue(CacheNode node) {
		CacheNode prevNode = node.getPrevNode();
		CacheNode nextNode = node.getNextNode();
		if (prevNode != null)
			prevNode.setNextNode(nextNode);
		if (nextNode != null)
			nextNode.setPrevNode(prevNode);

		nextNode = this.headNode.getNextNode();
		node.setPrevNode(this.headNode);
		node.setNextNode(nextNode);
		nextNode.setPrevNode(node);
		this.headNode.setNextNode(node);
	}

	private synchronized void cacheEviction() {
		CacheNode prevNode = this.tailNode;
		if (prevNode.getPrevNode() != null) {
			prevNode.getPrevNode().setNextNode(this.tailNode);
			this.tailNode.setPrevNode(prevNode.getPrevNode());
		}
	}

	public Value get(Key key) throws NoSuchElementException, ExecutionException, InterruptedException {
		Future<Value> value = executorService.submit(() -> {
			lock.lock();
			Value result = null;
			try {
				CacheNode node = this.nodeMap.get(key);
				result = node.getValue();
				this.reOrderQueue(node);
			} catch (NullPointerException ex) {
				result = new Value("key not found!!!");
			} finally {
				lock.unlock();
				return result;
			}
		});
		return value.get();
	}

	@Override public void put(Key key, Value value, Long ttl) {
		executorService.execute(() -> {
			lock.lock();
			try {
				CacheNode node = this.nodeMap.get(key);
				node.setValue(value);
				this.reOrderQueue(node);
			} catch (NullPointerException ex) {
				if (this.currSize.get() == this.cacheLimit)
					cacheEviction();
				CacheNode node = new CacheNode(key, value, ttl, nodeTimeUnit);
				this.nodeMap.put(key, node);
				this.reOrderQueue(node);
				this.currSize.compareAndSet(this.currSize.get(), this.currSize.get() + 1);
			} finally {
				lock.unlock();
			}
		});
	}

	@Override public Runnable cleanUp() {
		return (() -> {
			CacheNode itrNode = this.headNode.getNextNode();
			while (itrNode != tailNode) {
				if (itrNode.getExpiryTime() <= System.currentTimeMillis()) {
					CacheNode nextNode = itrNode.getNextNode();
					CacheNode prevNode = itrNode.getPrevNode();
					nextNode.setPrevNode(prevNode);
					prevNode.setNextNode(nextNode);
					this.nodeMap.remove(itrNode.getKey());
					this.currSize.getAndDecrement();
					System.out.println(itrNode.getKey().getKey() + " expired");
				}
				itrNode = itrNode.getNextNode();
			}
		});
	}
}
