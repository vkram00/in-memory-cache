package com.cache.redis.driver;

import com.cache.redis.models.KeyValueStore;

import java.util.concurrent.ExecutionException;

public class CacheDrive {

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		KeyValueStore kvStore = new KeyValueStore(4);

		kvStore.put("Key1", "Value1", 10L);
		kvStore.put("Key2", "Value2", 3L);
		kvStore.put("Key3", "Value3", 7L);
		kvStore.put("Key4", "Value4", 1L);
		kvStore.put("Key5", "Value5", 8L);
		Thread.sleep(2000);
		kvStore.put("Key6", "Value6", 8L);
		kvStore.put("Key7", "Value7", 8L);

		//Thread.sleep(2000);

		System.out.println(kvStore.get("Key3"));
		System.out.println(kvStore.get("Key5"));
		System.out.println(kvStore.get("Key4"));
	}
}
