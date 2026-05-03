package org.codelin.oauth.oauth.identity.domain.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社交登录提供商配置实体
 */
@Data
@Table("sys_social_provider_config")
public class SocialProviderConfig {

    @Id(keyType = KeyType.Generator, value = "snowflake")
    private Long id;

    private String provider;

    @Column("display_name")
    private String displayName;

    private Boolean enabled;

    @Column("client_id")
    private String clientId;

    @Column("client_secret")
    private String clientSecret;

    @Column("authorize_url")
    private String authorizeUrl;

    @Column("token_url")
    private String tokenUrl;

    @Column("token_method")
    private String tokenMethod;

    @Column("token_response_form_encoded")
    private Boolean tokenResponseFormEncoded;

    @Column("token_field_name")
    private String tokenFieldName;

    private String scope;

    @Column("userinfo_url")
    private String userinfoUrl;

    @Column("userinfo_method")
    private String userinfoMethod;

    @Column("userinfo_auth_style")
    private String userinfoAuthStyle;

    @Column("user_id_field")
    private String userIdField;

    @Column("nickname_field")
    private String nicknameField;

    @Column("avatar_field")
    private String avatarField;

    @Column("email_field")
    private String emailField;

    @Column("openid_enabled")
    private Boolean openidEnabled;

    @Column("openid_url")
    private String openidUrl;

    @Column("openid_field_name")
    private String openidFieldName;

    @Column("openid_response_field")
    private String openidResponseField;

    @Column("extra_authorize_params")
    private String extraAuthorizeParams;

    @Column("default_redirect_uri")
    private String defaultRedirectUri;

    @Column("sort_order")
    private Integer sortOrder;

    private Integer deleted;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
