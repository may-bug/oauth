package org.codelin.oauth.oauth.common.security;

/**
 * 安全上下文持有者 - 使用ThreadLocal存储当前请求的认证信息
 */
public class SecurityContextHolder {

    private static final ThreadLocal<OAuth2Authentication> CONTEXT = new ThreadLocal<>();

    public static void setAuthentication(OAuth2Authentication authentication) {
        CONTEXT.set(authentication);
    }

    public static OAuth2Authentication getAuthentication() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Long getUserId() {
        OAuth2Authentication auth = CONTEXT.get();
        return auth != null ? auth.getUserId() : null;
    }

    public static Long getOrgId() {
        OAuth2Authentication auth = CONTEXT.get();
        return auth != null ? auth.getOrgId() : null;
    }

    public static boolean isAuthenticated() {
        OAuth2Authentication auth = CONTEXT.get();
        return auth != null && auth.isAuthenticated();
    }
}
