package com.snowball.security;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int maxAttempts() default 5;
    int timeWindowSeconds() default 60;
}
