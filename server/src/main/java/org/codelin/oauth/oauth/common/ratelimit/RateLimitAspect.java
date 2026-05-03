package org.codelin.oauth.oauth.common.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.codelin.oauth.oauth.common.security.SecurityContextHolder;
import org.codelin.oauth.oauth.common.web.BizException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 限流切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitStore rateLimitStore;

    @Before("@annotation(org.codelin.oauth.oauth.common.ratelimit.RateLimiter)")
    public void doBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);

        if (rateLimiter == null) {
            return;
        }

        String key = generateKey(rateLimiter, method, joinPoint);

        boolean allowed = rateLimitStore.tryAcquire(
                key,
                rateLimiter.window(),
                rateLimiter.maxRequests()
        );

        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}", key);
            throw BizException.tooManyRequests(rateLimiter.message());
        }
    }

    /**
     * 生成限流key
     */
    private String generateKey(RateLimiter rateLimiter, Method method, JoinPoint joinPoint) {
        StringBuilder keyBuilder = new StringBuilder();

        // 添加key前缀
        if (rateLimiter.key() != null && !rateLimiter.key().isEmpty()) {
            keyBuilder.append(rateLimiter.key());
        } else {
            keyBuilder.append(method.getDeclaringClass().getSimpleName())
                    .append(":")
                    .append(method.getName());
        }

        // 根据限流类型添加后缀
        switch (rateLimiter.type()) {
            case USER:
                Long userId = SecurityContextHolder.getUserId();
                keyBuilder.append(":user:").append(userId != null ? userId : "anonymous");
                break;

            case API:
                ServletRequestAttributes attributes = (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    keyBuilder.append(":api:").append(request.getRequestURI());
                }
                break;

            case CUSTOM:
                // 自定义key，不添加额外后缀
                break;

            case DEFAULT:
            default:
                // 按IP限流
                attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String ip = getClientIp(request);
                    keyBuilder.append(":ip:").append(ip);
                }
                break;
        }

        return keyBuilder.toString();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
