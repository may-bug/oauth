package org.codelin.oauth.oauth.common.web;

/**
 * 请求上下文 - 使用ThreadLocal存储当前请求的用户和组织信息
 */
public class WebRequestContext {

    private static final ThreadLocal<RequestInfo> CURRENT = new ThreadLocal<>();

    public static void set(RequestInfo info) {
        CURRENT.set(info);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static RequestInfo get() {
        return CURRENT.get();
    }

    public static Long getUserId() {
        RequestInfo info = CURRENT.get();
        return info != null ? info.userId() : null;
    }

    public static Long getOrgId() {
        RequestInfo info = CURRENT.get();
        return info != null ? info.orgId() : null;
    }

    public static String getToken() {
        RequestInfo info = CURRENT.get();
        return info != null ? info.token() : null;
    }

    public static boolean isAuthenticated() {
        return getUserId() != null;
    }

    /**
     * 请求信息
     */
    public record RequestInfo(Long userId, Long orgId, String token) {

    }
}
