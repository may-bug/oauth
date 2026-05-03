package org.codelin.oauth.oauth.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.WebRequestContext;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 认证过滤器 - 解析token，设置SecurityContext
 * <p>
 * 不做任何拦截，仅尝试解析token并设置认证信息。
 * 认证/授权检查由 AuthorizationFilter 负责。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
@RequiredArgsConstructor
public class WebAuthenticationFilter extends OncePerRequestFilter {

    private final TokenStoreAdapter tokenStoreAdapter;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 提取token
        String token = extractToken(request);

        if (token != null) {
            try {
                // 验证token
                TokenInfo tokenInfo = tokenStoreAdapter.findToken(token);

                if (tokenInfo != null && !tokenInfo.isExpired()) {
                    // 构建认证对象
                    OAuth2Authentication auth = new OAuth2Authentication(
                            tokenInfo.getUserId(),
                            tokenInfo.getOrgId(),
                            token,
                            tokenInfo.getScopes(),
                            tokenInfo.getClientId()
                    );

                    SecurityContextHolder.setAuthentication(auth);

                    // 设置请求上下文
                    Long orgId = extractOrgId(request);
                    WebRequestContext.set(new WebRequestContext.RequestInfo(
                            tokenInfo.getUserId(),
                            orgId != null ? orgId : tokenInfo.getOrgId(),
                            token
                    ));
                }
                // token无效或过期时不设置认证信息，由AuthorizationFilter处理
            } catch (Exception e) {
                log.warn("Token validation failed: {}", e.getMessage());
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clear();
        }
    }

    /**
     * 提取token（Header > Cookie > Session > 查询参数）
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Authorization: Bearer <token>
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (!token.isEmpty()) {
                return token;
            }
        }

        // 2. Cookie（存的是裸 token）
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("Authorization".equals(cookie.getName())) {
                    String v = cookie.getValue();
                    if (v != null && !v.isEmpty()) {
                        return v;
                    }
                }
            }
        }

        // 3. HttpSession（存的是裸 token）
        var session = request.getSession(false);
        if (session != null) {
            Object sessionValue = session.getAttribute("Authorization");
            if (sessionValue instanceof String s && !s.isEmpty()) {
                return s;
            }
        }

        // 4. 查询参数（兼容）
        return request.getParameter("access_token");
    }

    /**
     * 提取组织ID
     */
    private Long extractOrgId(HttpServletRequest request) {
        String orgIdHeader = request.getHeader("X-Org-Id");
        if (orgIdHeader != null && !orgIdHeader.isEmpty()) {
            try {
                return Long.parseLong(orgIdHeader);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * Token存储适配器接口
     */
    public interface TokenStoreAdapter {
        TokenInfo findToken(String token);
    }
}
