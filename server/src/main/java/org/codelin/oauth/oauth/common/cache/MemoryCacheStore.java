package org.codelin.oauth.oauth.common.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 内存缓存实现
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "oauth.cache.type", havingValue = "memory", matchIfMissing = true)
public class MemoryCacheStore implements CacheStore {

    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            store.remove(key);
            return null;
        }
        return (T) entry.getValue();
    }

    @Override
    public void set(String key, Object value) {
        store.put(key, new CacheEntry(value, null));
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        Instant expiresAt = ttl != null ? Instant.now().plus(ttl) : null;
        store.put(key, new CacheEntry(value, expiresAt));
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }

    @Override
    public void delete(Collection<String> keys) {
        keys.forEach(store::remove);
    }

    @Override
    public boolean exists(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return false;
        }
        if (entry.isExpired()) {
            store.remove(key);
            return false;
        }
        return true;
    }

    @Override
    public boolean expire(String key, Duration ttl) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return false;
        }
        entry.setExpiresAt(Instant.now().plus(ttl));
        return true;
    }

    @Override
    public Long increment(String key) {
        return increment(key, 1);
    }

    @Override
    public Long increment(String key, long delta) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            AtomicLong value = new AtomicLong(delta);
            set(key, value);
            return value.get();
        }
        Object val = entry.getValue();
        if (val instanceof AtomicLong) {
            return ((AtomicLong) val).addAndGet(delta);
        }
        throw new IllegalArgumentException("Value at key " + key + " is not an AtomicLong");
    }

    @Override
    public Collection<String> keys(String pattern) {
        String regex = pattern.replace("*", ".*");
        return store.keySet().stream()
                .filter(key -> key.matches(regex))
                .collect(Collectors.toList());
    }

    /**
     * 定时清理过期缓存
     */
    @Scheduled(fixedRate = 60000)
    public void cleanExpired() {
        Instant now = Instant.now();
        Set<String> expiredKeys = store.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired(now))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (!expiredKeys.isEmpty()) {
            store.keySet().removeAll(expiredKeys);
            log.debug("Cleaned {} expired cache entries", expiredKeys.size());
        }
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        @Getter
        private final Object value;
        @Setter
        private Instant expiresAt;

        public CacheEntry(Object value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        public boolean isExpired() {
            return expiresAt != null && Instant.now().isAfter(expiresAt);
        }

        public boolean isExpired(Instant now) {
            return expiresAt != null && now.isAfter(expiresAt);
        }
    }
}
