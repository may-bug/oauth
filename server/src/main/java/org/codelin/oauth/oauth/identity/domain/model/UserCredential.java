package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户凭证实体
 */
@Data
@Table("sys_user_credential")
public class UserCredential {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    @Column("user_id")
    private Long userId;

    /**
     * 凭证类型: password, email, phone, social_github, social_gitee, social_qq
     */
    @Column("credential_type")
    private String credentialType;

    /**
     * 凭证键: 邮箱地址/手机号/social_uid
     */
    @Column("credential_key")
    private String credentialKey;

    /**
     * 凭证值: 密码hash/null
     */
    @Column("credential_value")
    private String credentialValue;

    /**
     * 是否已验证
     */
    private Boolean verified;

    /**
     * 是否主要凭证
     */
    @Column("primary_flag")
    private Boolean primaryFlag;

    @Column("created_at")
    private LocalDateTime createdAt;
}
