package org.codelin.oauth.oauth.rbac.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
@Table("sys_permission")
public class Permission {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private String code;

    private String name;

    private String description;

    /**
     * 类型: 1=菜单, 2=按钮, 3=API
     */
    private Integer type;

    @Column("parent_id")
    private Long parentId;

    /**
     * 菜单路由路径
     */
    private String path;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * API模式，如 GET:/users/**
     */
    @Column("api_pattern")
    private String apiPattern;

    @Column("sort_order")
    private Integer sortOrder;

    @Column("created_at")
    private LocalDateTime createdAt;
}
