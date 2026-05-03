package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 认证方式配置实体 (sys_auth_config)
 */
@Data
@Table("sys_auth_config")
public class AuthConfig {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    @Column("auth_type")
    private String authType;

    private Boolean enabled;

    private String config;

    private String description;

    private Integer deleted;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("updated_by")
    private Long updatedBy;
}
