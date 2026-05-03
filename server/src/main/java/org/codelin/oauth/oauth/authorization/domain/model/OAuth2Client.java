package org.codelin.oauth.oauth.authorization.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OAuth2客户端实体
 */
@Data
@Table("oauth2_client")
public class OAuth2Client {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    @Column("client_id")
    private String clientId;

    @Column("client_secret")
    private String clientSecret;

    @Column("client_name")
    private String clientName;

    /**
     * 客户端类型: 1=PUBLIC, 2=CONFIDENTIAL, 3=BEARER_ONLY
     */
    @Column("client_type")
    private Integer clientType;

    /**
     * 允许的回调地址（JSON数组）
     */
    @Column("redirect_uris")
    private String redirectUris;

    /**
     * 允许的scope（JSON数组）
     */
    @Column("allowed_scopes")
    private String allowedScopes;

    /**
     * 创建者用户ID
     */
    @Column("owner_id")
    private Long ownerId;

    /**
     * Access Token有效期（秒）
     */
    @Column("access_token_ttl")
    private Integer accessTokenTtl;

    /**
     * Refresh Token有效期（秒）
     */
    @Column("refresh_token_ttl")
    private Integer refreshTokenTtl;

    /**
     * 状态: 0=禁用, 1=正常
     */
    private Integer status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
