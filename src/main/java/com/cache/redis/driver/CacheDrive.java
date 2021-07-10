package com.cache.redis.driver;

import com.cache.redis.constants.CacheType;
import com.cache.redis.generics.Key;
import com.cache.redis.generics.Value;
import com.cache.redis.models.KeyValueStore;

import java.util.concurrent.ExecutionException;

public class CacheDrive {

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		KeyValueStore kvStore = new KeyValueStore(CacheType.LRU, 4);

		Key<String> key1 = new Key<>("Key1");
		Key<String> key2 = new Key<>("Key2");
		Key<String> key3 = new Key<>("Key3");
		Key<String> key4 = new Key<>("Key4");
		Key<String> key5 = new Key<>("Key5");
		Key<String> key6 = new Key<>("Key6");
		Key<String> key7 = new Key<>("Key7");

		Value<String> value1 = new Value<>("Value1");
		Value<String> value2 = new Value<>("Value2");
		Value<String> value3 = new Value<>("Value3");
		Value<String> value4 = new Value<>("Value4");
		Value<String> value5 = new Value<>("Value5");
		Value<String> value6 = new Value<>("Value6");
		Value<String> value7 = new Value<>("Value7");

		kvStore.put(key1, value1, 10L);
		kvStore.put(key2, value2, 3L);
		kvStore.put(key3, value3, 7L);
		kvStore.put(key4, value4, 1L);
		kvStore.put(key5, value5, 8L);
		Thread.sleep(2000);
		kvStore.put(key6, value6, 8L);
		kvStore.put(key7, value7, 8L);

		//Thread.sleep(2000);

		System.out.println(kvStore.get(key3).getValue());
		System.out.println(kvStore.get(key5).getValue());
		System.out.println(kvStore.get(key4).getValue());
	}
}
