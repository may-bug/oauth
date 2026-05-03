package org.codelin.oauth.oauth.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.store-type", havingValue = "redis")
public class RedisRateLimitStore implements RateLimitStore {

    private final RedissonClient redissonClient;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private static final String SLIDING_WINDOW_SCRIPT = """
        local key = KEYS[1]
        local window = tonumber(ARGV[1])
        local maxRequests = tonumber(ARGV[2])
        local now = tonumber(ARGV[3])
        local windowStart = now - window

        redis.call('ZREMRANGEBYSCORE', key, '-inf', windowStart)
        local currentCount = redis.call('ZCARD', key)

        if currentCount < maxRequests then
            redis.call('ZADD', key, now, now .. ':' .. math.random(1000000))
            redis.call('PEXPIRE', key, window)
            return 1
        else
            return 0
        end
        """;

    @Override
    public boolean tryAcquire(String key, int window, int maxRequests) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        long now = System.currentTimeMillis();

        Long result = redissonClient.getScript(LongCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                SLIDING_WINDOW_SCRIPT,
                RScript.ReturnType.LONG,
                Collections.singletonList(redisKey),
                window * 1000L, maxRequests, now
        );

        boolean allowed = result != null && result == 1;
        if (!allowed) log.debug("Rate limit exceeded for key: {}", key);
        return allowed;
    }

    @Override
    public long getCurrentCount(String key, int window) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        long now = System.currentTimeMillis();
        long windowStart = now - (window * 1000L);
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(redisKey);
        return zset.count(windowStart, true, now, true);
    }

    @Override
    public void reset(String key) {
        redissonClient.getKeys().delete(RATE_LIMIT_PREFIX + key);
    }
}
