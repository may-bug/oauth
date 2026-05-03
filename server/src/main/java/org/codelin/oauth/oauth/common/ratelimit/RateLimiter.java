package org.codelin.oauth.oauth.common.ratelimit;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流key前缀
     */
    String key() default "";

    /**
     * 时间窗口（秒）
     */
    int window() default 60;

    /**
     * 最大请求数
     */
    int maxRequests() default 100;

    /**
     * 限流类型
     */
    LimitType type() default LimitType.DEFAULT;

    /**
     * 限流提示消息
     */
    String message() default "error.too-many-requests";

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 默认（按IP限流）
         */
        DEFAULT,

        /**
         * 按用户限流
         */
        USER,

        /**
         * 按接口限流
         */
        API,

        /**
         * 自定义key
         */
        CUSTOM
    }
}
