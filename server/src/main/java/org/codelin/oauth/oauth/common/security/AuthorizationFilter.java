package org.codelin.oauth.oauth.common.security;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.common.web.R;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * 授权过滤器 - 检查@Anonymous、@Permission、@Role
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 300)
@RequiredArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping handlerMapping;
    private final PermissionResolver permissionResolver;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 获取HandlerMethod
        HandlerMethod handlerMethod = getHandlerMethod(request);
        if (handlerMethod == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Class<?> beanType = handlerMethod.getBeanType();

        // 1. 检查类级别 @Anonymous
        if (beanType.isAnnotationPresent(Anonymous.class)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 检查方法级别 @Anonymous
        if (handlerMethod.hasMethodAnnotation(Anonymous.class)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 以下都需要认证
        OAuth2Authentication auth = SecurityContextHolder.getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 401, "error.auth.login-required");
            return;
        }

        // 4. 检查方法级别 @Permission
        Permission methodPermission = handlerMethod.getMethodAnnotation(Permission.class);
        if (methodPermission != null) {
            if (!checkPermission(auth, methodPermission)) {
                writeResponse(response, HttpServletResponse.SC_FORBIDDEN, 403, "error.forbidden");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 检查类级别 @Permission
        Permission classPermission = beanType.getAnnotation(Permission.class);
        if (classPermission != null) {
            if (!checkPermission(auth, classPermission)) {
                writeResponse(response, HttpServletResponse.SC_FORBIDDEN, 403, "error.forbidden");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 6. 检查方法级别 @Role
        Role methodRole = handlerMethod.getMethodAnnotation(Role.class);
        if (methodRole != null) {
            if (!checkRole(auth, methodRole)) {
                writeResponse(response, HttpServletResponse.SC_FORBIDDEN, 403, "error.forbidden");
                return;
            }
        }

        // 7. 检查类级别 @Role
        Role classRole = beanType.getAnnotation(Role.class);
        if (classRole != null) {
            if (!checkRole(auth, classRole)) {
                writeResponse(response, HttpServletResponse.SC_FORBIDDEN, 403, "error.forbidden");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 获取HandlerMethod
     */
    private HandlerMethod getHandlerMethod(HttpServletRequest request) {
        try {
            HandlerExecutionChain chain = handlerMapping.getHandler(request);
            if (chain != null && chain.getHandler() instanceof HandlerMethod) {
                return (HandlerMethod) chain.getHandler();
            }
        } catch (Exception e) {
            log.debug("Failed to get handler method: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 检查权限
     */
    private boolean checkPermission(OAuth2Authentication auth, Permission permission) {
        Set<String> userPermissions = permissionResolver.getUserPermissions(
                auth.getUserId(), auth.getOrgId()
        );

        if (permission.logical() == Permission.Logical.AND) {
            return userPermissions.containsAll(Arrays.asList(permission.value()));
        } else {
            return Arrays.stream(permission.value())
                    .anyMatch(userPermissions::contains);
        }
    }

    /**
     * 检查角色
     */
    private boolean checkRole(OAuth2Authentication auth, Role role) {
        Set<String> userRoles = permissionResolver.getUserRoles(
                auth.getUserId(), auth.getOrgId()
        );

        return Arrays.stream(role.value())
                .anyMatch(userRoles::contains);
    }

    /**
     * 写入错误响应
     */
    private void writeResponse(HttpServletResponse response, int httpStatus, int bizCode, String messageKey) throws IOException {
        String message = messageSource.getMessage(
                messageKey, null, messageKey, LocaleContextHolder.getLocale());
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                R.error(bizCode, message)
        ));
    }

    /**
     * 权限解析器接口
     */
    public interface PermissionResolver {
        Set<String> getUserPermissions(Long userId, Long orgId);
        Set<String> getUserRoles(Long userId, Long orgId);
    }
}
