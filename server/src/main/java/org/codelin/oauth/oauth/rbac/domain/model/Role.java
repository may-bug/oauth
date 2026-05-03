package org.codelin.oauth.oauth.rbac.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@Table("sys_role")
public class Role {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private String code;

    private String name;

    private String description;

    /**
     * 组织ID，NULL表示系统角色
     */
    @Column("org_id")
    private Long orgId;

    /**
     * 是否系统内置
     */
    @Column("is_system")
    private Boolean isSystem;

    @Column("sort_order")
    private Integer sortOrder;

    /**
     * 状态: 0=禁用, 1=正常
     */
    private Integer status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
