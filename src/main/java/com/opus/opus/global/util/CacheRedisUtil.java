package com.opus.opus.global.util;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CacheRedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheRedisUtil(@Qualifier("cacheRedisTemplate") final RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(final String key, final Object value, final long timeout, final TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(final String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
