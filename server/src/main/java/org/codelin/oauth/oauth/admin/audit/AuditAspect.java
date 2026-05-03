package org.codelin.oauth.oauth.admin.audit;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.codelin.oauth.oauth.admin.domain.model.AuditLog;
import org.codelin.oauth.oauth.admin.infrastructure.persistence.AuditLogMapper;
import org.codelin.oauth.oauth.common.security.SecurityContextHolder;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 审计日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    private final ExpressionParser parser = new SpelExpressionParser();

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Auditable auditable, Object result) {
        saveAuditLog(joinPoint, auditable, 1, null);
    }

    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
    public void doAfterThrowing(JoinPoint joinPoint, Auditable auditable, Throwable ex) {
        saveAuditLog(joinPoint, auditable, 0, ex.getMessage());
    }

    /**
     * 保存审计日志
     */
    private void saveAuditLog(JoinPoint joinPoint, Auditable auditable, int status, String errorMsg) {
        try {
            AuditLog auditLog = new AuditLog();

            // 设置基本信息
            auditLog.setAction(auditable.action());
            auditLog.setResourceType(auditable.resourceType());
            auditLog.setStatus(status);
            auditLog.setErrorMsg(errorMsg);
            auditLog.setCreatedAt(LocalDateTime.now());

            // 设置用户信息
            Long userId = SecurityContextHolder.getUserId();
            if (userId != null) {
                auditLog.setUserId(userId);
            }

            // 设置请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIp(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setTraceId(request.getHeader("X-Trace-Id"));
            }

            // 解析资源ID
            if (auditable.resourceId() != null && !auditable.resourceId().isEmpty()) {
                String resourceId = parseResourceId(auditable.resourceId(), joinPoint);
                auditLog.setResourceId(resourceId);
            }

            // 记录请求参数
            if (auditable.logParams()) {
                try {
                    String params = objectMapper.writeValueAsString(joinPoint.getArgs());
                    auditLog.setDetail(params);
                } catch (Exception e) {
                    log.warn("Failed to serialize audit params", e);
                }
            }

            // 异步保存
            auditLogMapper.insert(auditLog);
            log.debug("Audit log saved: action={}, resource={}", auditable.action(), auditable.resourceType());

        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    /**
     * 解析资源ID（支持SpEL表达式）
     */
    private String parseResourceId(String expression, JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }

            Object value = parser.parseExpression(expression).getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to parse resource ID expression: {}", expression, e);
            return null;
        }
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
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
