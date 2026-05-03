package org.codelin.oauth.oauth.common.cache;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "oauth.cache.type", havingValue = "redis")
@RequiredArgsConstructor
public class RedisCacheStore implements CacheStore {

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "oauth:cache:";

    private String key(String k) { return KEY_PREFIX + k; }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key(key));
        String json = bucket.get();
        if (json == null) return null;
        if (isSimpleType(json)) return (T) json;
        return null;
    }

    @Override
    public void set(String key, Object value) {
        redissonClient.getBucket(key(key)).set(toJson(value));
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        redissonClient.getBucket(key(key)).set(toJson(value), ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void delete(String key) {
        redissonClient.getBucket(key(key)).delete();
    }

    @Override
    public void delete(Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            redissonClient.getKeys().delete(keys.stream().map(this::key).toArray(String[]::new));
        }
    }

    @Override
    public boolean exists(String key) {
        return redissonClient.getKeys().countExists(key(key)) > 0;
    }

    @Override
    public boolean expire(String key, Duration ttl) {
        return redissonClient.getBucket(key(key)).expire(ttl);
    }

    @Override
    public Long increment(String key) {
        return increment(key, 1);
    }

    @Override
    public Long increment(String key, long delta) {
        RAtomicLong atomic = redissonClient.getAtomicLong(key(key));
        return atomic.addAndGet(delta);
    }

    @Override
    public Collection<String> keys(String pattern) {
        RKeys rKeys = redissonClient.getKeys();
        Iterable<String> matched = rKeys.getKeysByPattern(KEY_PREFIX + pattern);
        Set<String> result = new java.util.HashSet<>();
        matched.forEach(k -> result.add(k.substring(KEY_PREFIX.length())));
        return result;
    }

    private String toJson(Object value) {
        if (value instanceof String || value instanceof Number || value instanceof Boolean)
            return String.valueOf(value);
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize value for Redis cache", e);
            return String.valueOf(value);
        }
    }

    private boolean isSimpleType(String json) {
        return !(json.startsWith("{") || json.startsWith("[") || json.startsWith("\""));
    }
}
