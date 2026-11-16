package com.opus.opus.global.util;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class AuthRedisUtil {

    private static final RedisScript<Long> DECREMENT_IF_EXISTS_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('EXISTS', KEYS[1]) == 1 then return redis.call('DECR', KEYS[1]) end return nil",
            Long.class);

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

    public void decrementIfExists(final String key) {
        redisTemplate.execute(DECREMENT_IF_EXISTS_SCRIPT, List.of(key));
    }
}
