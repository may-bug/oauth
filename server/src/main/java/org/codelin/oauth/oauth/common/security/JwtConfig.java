package org.codelin.oauth.oauth.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置
 */
@Data
@Component
@ConfigurationProperties("app.jwt")
public class JwtConfig {

    /** HS256 签名密钥，为空时自动生成 */
    private String secret;

    /** Access Token 有效期（秒），默认 1 小时 */
    private long accessTokenTtl = 3600;

    /** Refresh Token 有效期（秒），默认 24 小时 */
    private long refreshTokenTtl = 86400;
}
