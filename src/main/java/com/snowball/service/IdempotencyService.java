package com.snowball.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based idempotency token service.
 *
 * Flow:
 * 1. Frontend requests a token before submitting (e.g., when opening a form).
 * 2. Frontend includes the token in the request header (X-Idempotency-Key).
 * 3. Backend checks: if token exists in Redis → 409 duplicate; else → store + proceed.
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final StringRedisTemplate redis;

    public IdempotencyService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static final String PREFIX = "idempotency:";
    private static final long TOKEN_TTL_HOURS = 24;

    /**
     * Generate a new idempotency token. Used by the frontend before submitting.
     */
    public String generateToken(Long userId, String action) {
        String token = UUID.randomUUID().toString();
        String key = PREFIX + userId + ":" + action;
        redis.opsForValue().set(key, token, TOKEN_TTL_HOURS, TimeUnit.HOURS);
        return token;
    }

    /**
     * Check and consume the idempotency token.
     * @return true if the token is valid (first submission), false if duplicate.
     */
    public boolean checkAndConsume(Long userId, String action, String token) {
        if (token == null || token.isBlank()) return true; // no token → allow
        String key = PREFIX + userId + ":" + action;
        String stored = redis.opsForValue().get(key);
        if (stored == null) return true; // no stored token → allow
        if (!stored.equals(token)) return false; // mismatch → duplicate

        // Valid match → consume (delete) to prevent reuse
        redis.delete(key);
        return true;
    }
}
