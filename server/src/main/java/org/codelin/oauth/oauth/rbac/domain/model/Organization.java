package org.codelin.oauth.oauth.rbac.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组织实体
 */
@Data
@Table("sys_organization")
public class Organization {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private String name;

    private String code;

    @Column("parent_id")
    private Long parentId;

    /**
     * 状态: 0=禁用, 1=正常
     */
    private Integer status;

    @Column("sort_order")
    private Integer sortOrder;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
