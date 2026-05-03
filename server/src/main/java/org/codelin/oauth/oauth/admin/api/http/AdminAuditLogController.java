package org.codelin.oauth.oauth.admin.api.http;

import lombok.RequiredArgsConstructor;
import org.codelin.oauth.oauth.admin.application.AuditLogService;
import org.codelin.oauth.oauth.admin.domain.model.AuditLog;
import org.codelin.oauth.oauth.common.security.Permission;
import org.codelin.oauth.oauth.common.web.BizException;
import org.codelin.oauth.oauth.common.web.R;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    @Permission("audit:read")
    @GetMapping(version = "1")
    public ResponseEntity<R<R.PageResult<AuditLog>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<AuditLog> logs = auditLogService.list(page, size, userId, action, resourceType, status, startTime, endTime);
        long total = auditLogService.count(userId, action, resourceType, status, startTime, endTime);
        return R.okPage(logs, total, page, size);
    }

    @Permission("audit:read")
    @GetMapping(value = "/{id}", version = "1")
    public ResponseEntity<R<AuditLog>> getById(@PathVariable Long id) {
        AuditLog log = auditLogService.getById(id);
        if (log == null) throw BizException.notFound("error.not-found");
        return R.ok(log);
    }
}
