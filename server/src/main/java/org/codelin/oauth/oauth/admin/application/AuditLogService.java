package org.codelin.oauth.oauth.admin.application;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.admin.domain.model.AuditLog;
import org.codelin.oauth.oauth.admin.infrastructure.persistence.AuditLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    public List<AuditLog> list(int page, int size, Long userId, String action,
                                String resourceType, Integer status,
                                LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = buildQuery(userId, action, resourceType, status, startTime, endTime);
        return auditLogMapper.selectListByQuery(
                query.orderBy("created_at", false).limit(size).offset((page - 1) * size));
    }

    public long count(Long userId, String action, String resourceType,
                      Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.selectCountByQuery(
                buildQuery(userId, action, resourceType, status, startTime, endTime));
    }

    public AuditLog getById(Long id) {
        return auditLogMapper.selectOneById(id);
    }

    private QueryWrapper buildQuery(Long userId, String action, String resourceType,
                                     Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create();
        if (userId != null) query.eq("user_id", userId);
        if (action != null && !action.isEmpty()) query.eq("action", action);
        if (resourceType != null && !resourceType.isEmpty()) query.eq("resource_type", resourceType);
        if (status != null) query.eq("status", status);
        if (startTime != null) query.ge("created_at", startTime);
        if (endTime != null) query.le("created_at", endTime);
        return query;
    }
}
