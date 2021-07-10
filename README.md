#In-Memory-Cache
* This is an in-memory implementation of key-value cache store like redis.
* Each key-value pair can be stored in the cache with a time-to-live parameter. This decides how long the corresponding pair will be stored in the cache.
* Key and Value are implemented using Java generics. This means that the same cache store supports different types of keys as well as values.
* This implementation also takes care of the concurrency.
* User can choose the cache type while initialising the key-value store.
* Supported cache types are - 
    * LRU
    * LFU
* User should also specify the size of the key-value store, which when crossed the cache eviction happens.
 
