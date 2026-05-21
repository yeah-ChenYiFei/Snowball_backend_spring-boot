package com.snowball.security;

import com.snowball.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static class Counter {
        long windowStart = System.currentTimeMillis();
        int count = 0;
    }

    private final Map<String, Counter> store = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod hm))
            return true;

        RateLimit annotation = hm.getMethodAnnotation(RateLimit.class);
        if (annotation == null)
            return true;

        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        long now = System.currentTimeMillis();
        long windowMs = annotation.timeWindowSeconds() * 1000L;

        Counter counter = store.compute(key, (k, v) -> {
            if (v == null || now - v.windowStart > windowMs)
                return new Counter();
            return v;
        });

        synchronized (counter) {
            if (now - counter.windowStart > windowMs) {
                counter.windowStart = now;
                counter.count = 0;
            }
            counter.count++;
            if (counter.count > annotation.maxAttempts()) {
                throw new BusinessException(429, "请求过于频繁，请稍后再试");
            }
        }

        return true;
    }
}
