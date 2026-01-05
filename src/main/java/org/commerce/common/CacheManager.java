package org.commerce.common;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Generic cache manager with TTL (Time-To-Live) and LRU eviction support.
 * Thread-safe implementation for caching frequently accessed data.
 */
public class CacheManager<K, V> {
    
    private final Map<K, CacheEntry<V>> cache;
    private final long ttlMillis;
    private final int maxSize;
    
    /**
     * Creates a cache manager with TTL and max size.
     * 
     * @param ttlMillis Time-to-live in milliseconds (0 for no expiration)
     * @param maxSize Maximum cache size (uses LRU eviction when exceeded)
     */
    public CacheManager(long ttlMillis, int maxSize) {
        this.ttlMillis = ttlMillis;
        this.maxSize = maxSize;
        
        // Thread-safe cache with LRU eviction
        this.cache = new ConcurrentHashMap<>();
    }
    
    /**
     * Gets a value from cache or computes it if not present/expired.
     * 
     * @param key The cache key
     * @param supplier Function to compute value if not cached
     * @return The cached or computed value
     */
    public V get(K key, Supplier<V> supplier) {
        CacheEntry<V> entry = cache.get(key);
        
        // Check if cache hit and not expired
        if (entry != null && !isExpired(entry)) {
            entry.updateAccessTime();
            return entry.getValue();
        }
        
        // Cache miss or expired - compute and store
        V value = supplier.get();
        put(key, value);
        return value;
    }
    
    /**
     * Gets a value from cache without computing.
     * 
     * @param key The cache key
     * @return Optional containing the value if present and not expired
     */
    public Optional<V> getIfPresent(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && !isExpired(entry)) {
            entry.updateAccessTime();
            return Optional.of(entry.getValue());
        }
        return Optional.empty();
    }
    
    /**
     * Puts a value into cache.
     * 
     * @param key The cache key
     * @param value The value to cache
     */
    public void put(K key, V value) {
        // Enforce size limit with LRU eviction
        if (cache.size() >= maxSize) {
            evictLRU();
        }
        
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
    }
    
    /**
     * Invalidates (removes) a specific cache entry.
     * 
     * @param key The cache key to invalidate
     */
    public void invalidate(K key) {
        cache.remove(key);
    }
    
    /**
     * Clears all cache entries.
     */
    public void invalidateAll() {
        cache.clear();
    }
    
    /**
     * Gets the current cache size.
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Checks if a cache entry is expired.
     */
    private boolean isExpired(CacheEntry<V> entry) {
        if (ttlMillis <= 0) {
            return false; // No expiration
        }
        return (System.currentTimeMillis() - entry.getCreationTime()) > ttlMillis;
    }
    
    /**
     * Evicts the least recently used entry.
     */
    private void evictLRU() {
        K oldestKey = null;
        long oldestAccessTime = Long.MAX_VALUE;
        
        for (Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            long accessTime = entry.getValue().getLastAccessTime();
            if (accessTime < oldestAccessTime) {
                oldestAccessTime = accessTime;
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cache.remove(oldestKey);
        }
    }
    
    /**
     * Removes expired entries from cache.
     */
    public void cleanUp() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }
    
    /**
     * Inner class representing a cache entry with timestamps.
     */
    private static class CacheEntry<V> {
        private final V value;
        private final long creationTime;
        private volatile long lastAccessTime;
        
        public CacheEntry(V value, long creationTime) {
            this.value = value;
            this.creationTime = creationTime;
            this.lastAccessTime = creationTime;
        }
        
        public V getValue() {
            return value;
        }
        
        public long getCreationTime() {
            return creationTime;
        }
        
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        public void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}
