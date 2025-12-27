package com.opus.opus.global.util;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthRedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    public AuthRedisUtil(@Qualifier("authRedisTemplate") final RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(final String key, final String value, final long timeout, final TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String get(final String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean exists(final String key) {
        return Objects.equals(Boolean.TRUE, redisTemplate.hasKey(key));
    }

    public boolean delete(final String key) {
        return Objects.equals(Boolean.TRUE, redisTemplate.delete(key));
    }

    public Long ttl(final String key, final TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    public Long incrementWithTtl(final String key, final long timeout, final TimeUnit unit) {
        final Long value = redisTemplate.opsForValue().increment(key);
        if (value == 1L && timeout > 0) {
            redisTemplate.expire(key, timeout, unit);
        }
        return value;
    }
}
