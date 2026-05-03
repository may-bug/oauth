package org.codelin.oauth.oauth.common.ratelimit;

/**
 * 限流存储接口
 */
public interface RateLimitStore {

    /**
     * 尝试获取令牌（滑动窗口算法）
     *
     * @param key         限流key
     * @param window      时间窗口（秒）
     * @param maxRequests 最大请求数
     * @return true=允许请求, false=限流
     */
    boolean tryAcquire(String key, int window, int maxRequests);

    /**
     * 获取当前窗口的请求数
     *
     * @param key    限流key
     * @param window 时间窗口（秒）
     * @return 当前请求数
     */
    long getCurrentCount(String key, int window);

    /**
     * 重置限流计数
     *
     * @param key 限流key
     */
    void reset(String key);
}
