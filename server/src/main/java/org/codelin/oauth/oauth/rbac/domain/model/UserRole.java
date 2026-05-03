package org.codelin.oauth.oauth.rbac.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("sys_user_role")
public class UserRole {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("role_id")
    private Long roleId;

    @Column("org_id")
    private Long orgId;

    @Column("created_at")
    private LocalDateTime createdAt;
}
