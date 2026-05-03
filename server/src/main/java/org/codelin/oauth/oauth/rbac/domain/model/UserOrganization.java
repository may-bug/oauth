package org.codelin.oauth.oauth.rbac.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户组织关联实体
 */
@Data
@Table("sys_user_organization")
public class UserOrganization {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("org_id")
    private Long orgId;

    /**
     * 在该组织下的主角色
     */
    @Column("role_id")
    private Long roleId;

    /**
     * 状态: 0=待审批, 1=正常, 2=拒绝
     */
    private Integer status;

    @Column("joined_at")
    private LocalDateTime joinedAt;
}
