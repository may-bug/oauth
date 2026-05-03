package org.codelin.oauth.oauth.common.web;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;
    private final String messageKey;
    private final Object[] args;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
        this.messageKey = null;
        this.args = null;
    }

    public BizException(int code, String messageKey, Object... args) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
        this.args = args;
    }

    // ========== 常用异常工厂方法（code 与 HTTP 状态码一致）==========

    public static BizException badRequest(String messageKey, Object... args) {
        return new BizException(400, messageKey, args);
    }

    public static BizException unauthorized(String messageKey, Object... args) {
        return new BizException(401, messageKey, args);
    }

    public static BizException forbidden(String messageKey, Object... args) {
        return new BizException(403, messageKey, args);
    }

    public static BizException notFound(String messageKey, Object... args) {
        return new BizException(404, messageKey, args);
    }

    public static BizException conflict(String messageKey, Object... args) {
        return new BizException(409, messageKey, args);
    }

    public static BizException tooManyRequests(String messageKey, Object... args) {
        return new BizException(429, messageKey, args);
    }

    public static BizException serverError(String messageKey, Object... args) {
        return new BizException(500, messageKey, args);
    }
}
