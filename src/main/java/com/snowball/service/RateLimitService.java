package com.snowball.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Redis sliding-window rate limiter.
 * Uses sorted sets to implement per-IP, per-action rate limiting.
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private final StringRedisTemplate redis;

    @Value("${snowball.rate-limit.post-per-minute:3}")
    private int postPerMinute;

    @Value("${snowball.rate-limit.comment-per-minute:10}")
    private int commentPerMinute;

    public RateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static final String PREFIX = "ratelimit:";

    public record RateLimitResult(boolean allowed, long retryAfterSeconds) {}

    /**
     * Check if the action for this IP is within the rate limit.
     * Uses sliding window: stores timestamps in a sorted set, removes expired entries.
     */
    private RateLimitResult check(String ip, String action, int maxRequests, int windowSeconds) {
        String key = PREFIX + action + ":" + ip;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (windowSeconds * 1000L);

        // Remove expired entries (Atomic)
        redis.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // Count remaining entries
        Long count = redis.opsForZSet().zCard(key);

        if (count != null && count >= maxRequests) {
            // Get the earliest entry to calculate retry time
            var earliest = redis.opsForZSet().rangeWithScores(key, 0, 0);
            long retryAfter = 1;
            if (earliest != null && !earliest.isEmpty()) {
                long earliestTime = earliest.iterator().next().getScore().longValue();
                retryAfter = Math.max(1, (earliestTime / 1000) + windowSeconds - (now / 1000));
            }
            return new RateLimitResult(false, retryAfter);
        }

        // Add current timestamp
        redis.opsForZSet().add(key, String.valueOf(now), now);
        redis.expire(key, windowSeconds + 1, TimeUnit.SECONDS);

        return new RateLimitResult(true, 0);
    }

    public RateLimitResult checkPostLimit(String ip) {
        return check(ip, "post", postPerMinute, 60);
    }

    public RateLimitResult checkCommentLimit(String ip) {
        return check(ip, "comment", commentPerMinute, 60);
    }
}
