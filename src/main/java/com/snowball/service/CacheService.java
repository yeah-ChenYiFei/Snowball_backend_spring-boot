package com.snowball.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Cache-Aside pattern with:
 * - Read: check cache → return if hit → query DB → store in cache → return
 * - Write: update DB → delete cache → delayed double-delete (500ms)
 * - Null caching: cache empty values for 1 minute to prevent cache penetration
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    public CacheService(StringRedisTemplate redis, ObjectMapper om) {
        this.redis = redis;
        this.om = om;
    }

    private static final long DEFAULT_TTL_SECONDS = 300; // 5 minutes
    private static final long NULL_TTL_SECONDS = 60;     // 1 minute for null cache
    private static final String NULL_MARKER = "__NULL__";

    /* ---- Read ---- */

    public <T> T getOrLoad(String cacheKey, Class<T> clazz, long ttlSeconds, Supplier<T> loader) {
        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            if (NULL_MARKER.equals(cached)) {
                log.debug("Cache hit (null marker): {}", cacheKey);
                return null;
            }
            try {
                T value = om.readValue(cached, clazz);
                log.debug("Cache hit: {}", cacheKey);
                return value;
            } catch (JsonProcessingException e) {
                log.warn("Cache deserialize error for key {}: {}", cacheKey, e.getMessage());
                redis.delete(cacheKey);
            }
        }

        // Load from DB
        T value = loader.get();
        if (value != null) {
            try {
                redis.opsForValue().set(cacheKey, om.writeValueAsString(value), ttlSeconds, TimeUnit.SECONDS);
            } catch (JsonProcessingException e) {
                log.warn("Cache serialize error for key {}: {}", cacheKey, e.getMessage());
            }
        } else {
            // Store null marker to prevent cache penetration
            redis.opsForValue().set(cacheKey, NULL_MARKER, NULL_TTL_SECONDS, TimeUnit.SECONDS);
        }
        return value;
    }

    /* ---- Shortcut ---- */

    public <T> T getOrLoad(String cacheKey, Class<T> clazz, Supplier<T> loader) {
        return getOrLoad(cacheKey, clazz, DEFAULT_TTL_SECONDS, loader);
    }

    /* ---- Invalidate ---- */

    public void evict(String cacheKey) {
        redis.delete(cacheKey);
        log.debug("Cache evicted: {}", cacheKey);
    }

    /**
     * Delayed double-delete to handle race conditions during concurrent reads.
     * First delete happens before DB write (caller responsibility).
     * This method should be called after DB commit.
     */
    @Async
    public void delayedDoubleDelete(String cacheKey, long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        redis.delete(cacheKey);
        log.debug("Delayed double-delete for: {}", cacheKey);
    }

    public void delayedDoubleDelete(String cacheKey) {
        delayedDoubleDelete(cacheKey, 500);
    }

    /* ---- Counter cache ---- */

    public Long getOrLoadCounter(String cacheKey, long ttlSeconds, Supplier<Long> loader) {
        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            if (NULL_MARKER.equals(cached)) return 0L;
            try { return Long.parseLong(cached); } catch (NumberFormatException e) {
                redis.delete(cacheKey);
            }
        }
        Long value = loader.get();
        value = value != null ? value : 0L;
        redis.opsForValue().set(cacheKey, String.valueOf(value), ttlSeconds, TimeUnit.SECONDS);
        return value;
    }
}
