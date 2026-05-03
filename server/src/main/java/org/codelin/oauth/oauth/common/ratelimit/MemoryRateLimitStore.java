package org.codelin.oauth.oauth.common.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 内存限流存储实现（滑动窗口算法）
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.rate-limit.store-type", havingValue = "memory", matchIfMissing = true)
public class MemoryRateLimitStore implements RateLimitStore {

    /**
     * 存储结构: key -> 时间戳队列
     */
    private final ConcurrentHashMap<String, Deque<Long>> store = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int window, int maxRequests) {
        long now = System.currentTimeMillis();
        long windowStart = now - (window * 1000L);

        Deque<Long> timestamps = store.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // 移除窗口外的时间戳
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        // 检查是否超过限制
        if (timestamps.size() >= maxRequests) {
            log.debug("Rate limit exceeded for key: {}, current: {}, max: {}",
                    key, timestamps.size(), maxRequests);
            return false;
        }

        // 添加当前时间戳
        timestamps.addLast(now);
        return true;
    }

    @Override
    public long getCurrentCount(String key, int window) {
        long now = System.currentTimeMillis();
        long windowStart = now - (window * 1000L);

        Deque<Long> timestamps = store.get(key);
        if (timestamps == null) {
            return 0;
        }

        // 移除窗口外的时间戳
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        return timestamps.size();
    }

    @Override
    public void reset(String key) {
        store.remove(key);
    }

    /**
     * 定期清理过期数据
     */
    @Scheduled(fixedRate = 60000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        long maxWindow = 3600 * 1000L; // 最大1小时窗口

        int removed = 0;
        for (Map.Entry<String, Deque<Long>> entry : store.entrySet()) {
            Deque<Long> timestamps = entry.getValue();
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - maxWindow) {
                timestamps.pollFirst();
                removed++;
            }
            // 移除空队列
            if (timestamps.isEmpty()) {
                store.remove(entry.getKey());
            }
        }

        if (removed > 0) {
            log.debug("Cleaned up {} expired rate limit entries", removed);
        }
    }
}
