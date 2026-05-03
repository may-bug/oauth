package org.codelin.oauth.oauth.common.cache;

import java.time.Duration;
import java.util.Collection;

/**
 * 缓存存储接口
 */
public interface CacheStore {

    /**
     * 获取缓存
     */
    <T> T get(String key);

    /**
     * 设置缓存（永不过期）
     */
    void set(String key, Object value);

    /**
     * 设置缓存（指定过期时间）
     */
    void set(String key, Object value, Duration ttl);

    /**
     * 删除缓存
     */
    void delete(String key);

    /**
     * 批量删除
     */
    void delete(Collection<String> keys);

    /**
     * 检查key是否存在
     */
    boolean exists(String key);

    /**
     * 设置过期时间
     */
    boolean expire(String key, Duration ttl);

    /**
     * 自增
     */
    Long increment(String key);

    /**
     * 自增指定值
     */
    Long increment(String key, long delta);

    /**
     * 获取匹配的keys
     */
    Collection<String> keys(String pattern);
}
