package com.cache.redis.models;

import com.cache.redis.interfaces.IKeyValueStore;

import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class KeyValueStore implements IKeyValueStore {

	private final static TimeUnit nodeTimeUnit = TimeUnit.SECONDS;
	private final int storeLimit;
	private AtomicInteger currSize;
	private CacheNode headNode;
	private CacheNode tailNode;
	private final ConcurrentHashMap<String, CacheNode> nodeMap;
	private final ReentrantLock lock;
	private final ExecutorService executorService;
	private final ScheduledExecutorService cleanUpService;

	public KeyValueStore(int storeLimit) {
		this.headNode = new CacheNode("dummyHeadNode", "dummyHeadValue", Long.MAX_VALUE, nodeTimeUnit);
		this.tailNode = new CacheNode("dummyTailNode", "dummyTailValue", Long.MAX_VALUE, nodeTimeUnit);
		this.headNode.setNextNode(this.tailNode);
		this.tailNode.setPrevNode(this.headNode);

		this.nodeMap = new ConcurrentHashMap<>();
		this.lock = new ReentrantLock();
		this.storeLimit = storeLimit;
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

	public String get(String key) throws NoSuchElementException, ExecutionException, InterruptedException {
		Future<String> value = executorService.submit(() -> {
			lock.lock();
			String result = "";
			try {
				CacheNode node = this.nodeMap.get(key);
				result = node.getValue();
				this.reOrderQueue(node);
				return result;
			} catch (NullPointerException ex) {
				System.out.println("key not found!!!");
			} finally {
				lock.unlock();
				return result;
			}
		});
		return value.get();
	}

	public void put(String key, String value, Long ttl) {
		executorService.execute(() -> {
			lock.lock();
			try {
				CacheNode node = this.nodeMap.get(key);
				node.setValue(value);
				this.reOrderQueue(node);
			} catch (NullPointerException ex) {
				if (this.currSize.get() == this.storeLimit)
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
					System.out.println(itrNode.getKey() + " expired");
				}
				itrNode = itrNode.getNextNode();
			}
		});
	}
}
