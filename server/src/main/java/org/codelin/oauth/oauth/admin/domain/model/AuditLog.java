package org.codelin.oauth.oauth.admin.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 */
@Data
@Table("sys_audit_log")
public class AuditLog {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    @Column("trace_id")
    private String traceId;

    @Column("user_id")
    private Long userId;

    private String username;

    @Column("org_id")
    private Long orgId;

    private String action;

    @Column("resource_type")
    private String resourceType;

    @Column("resource_id")
    private String resourceId;

    private String detail;

    private String ip;

    @Column("user_agent")
    private String userAgent;

    /**
     * 状态: 1=成功, 0=失败
     */
    private Integer status;

    @Column("error_msg")
    private String errorMsg;

    @Column("created_at")
    private LocalDateTime createdAt;
}
