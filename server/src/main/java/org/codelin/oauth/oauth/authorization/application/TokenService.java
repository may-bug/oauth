package org.codelin.oauth.oauth.authorization.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.authorization.infrastructure.token.TokenStore;
import org.codelin.oauth.oauth.common.security.JwtConfig;
import org.codelin.oauth.oauth.common.security.JwtService;
import org.codelin.oauth.oauth.common.security.TokenInfo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

/**
 * Token服务 - 生成、管理Token（Access Token 为 JWT，Refresh Token 为不透明 UUID）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenStore tokenStore;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;

    /**
     * 生成 JWT Access Token
     */
    public TokenInfo generateAccessToken(Long userId, Long orgId, String clientId, Set<String> scopes) {
        return generateAccessToken(userId, orgId, clientId, scopes, (int) jwtConfig.getAccessTokenTtl());
    }

    /**
     * 生成 JWT Access Token（自定义TTL）
     */
    public TokenInfo generateAccessToken(Long userId, Long orgId, String clientId,
                                          Set<String> scopes, int ttlSeconds) {
        String token = jwtService.generateToken(userId, orgId, clientId, scopes, ttlSeconds);

        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setToken(token);
        tokenInfo.setUserId(userId);
        tokenInfo.setOrgId(orgId);
        tokenInfo.setClientId(clientId);
        tokenInfo.setScopes(scopes);
        tokenInfo.setExpiresAt(Instant.now().plus(ttlSeconds, ChronoUnit.SECONDS));

        // JWT 是自包含的，但仍存储以便 revokeUserTokens 等批量操作
        tokenStore.saveToken(tokenInfo);

        log.debug("JWT access token generated for user: {}, client: {}", userId, clientId);
        return tokenInfo;
    }

    /**
     * 生成 Refresh Token（不透明 UUID，存储在服务端）
     */
    public String generateRefreshToken(Long userId, String clientId, Set<String> scopes,
                                        String accessToken) {
        String refreshToken = generateTokenString();

        TokenStore.RefreshTokenInfo info = new TokenStore.RefreshTokenInfo(
                refreshToken, accessToken, clientId, userId, scopes
        );

        tokenStore.saveRefreshToken(refreshToken, info);

        log.debug("Refresh token generated for user: {}, client: {}", userId, clientId);
        return refreshToken;
    }

    /**
     * 生成授权码
     */
    public String generateAuthorizationCode(String clientId, Long userId,
                                              String redirectUri, Set<String> scopes, String state) {
        String code = generateTokenString();

        TokenStore.AuthorizationCodeInfo info = new TokenStore.AuthorizationCodeInfo(
                code, clientId, userId, redirectUri, scopes, state
        );

        tokenStore.saveAuthorizationCode(code, info);

        log.debug("Authorization code generated for user: {}, client: {}", userId, clientId);
        return code;
    }

    /**
     * 验证并消费授权码
     */
    public TokenStore.AuthorizationCodeInfo consumeAuthorizationCode(String code) {
        return tokenStore.consumeAuthorizationCode(code);
    }

    /**
     * 验证 Access Token（JWT 自校验 + 撤销检查）
     * <p>
     * 签发时写入 store，撤销时移除。store 中没有 = 已撤销或未签发。
     */
    public TokenInfo validateAccessToken(String token) {
        // JWT 自校验（签名 + 过期）
        TokenInfo info = jwtService.parseToken(token);
        if (info == null) {
            return null;
        }
        // 签发过的 token 一定在 store 中；被撤销的已移除
        TokenInfo stored = tokenStore.findToken(token);
        return stored != null ? info : null;
    }

    /**
     * 验证 Refresh Token
     */
    public TokenStore.RefreshTokenInfo validateRefreshToken(String refreshToken) {
        return tokenStore.findRefreshToken(refreshToken);
    }

    /**
     * 撤销 Token（从 store 中移除）
     */
    public void revokeToken(String token) {
        tokenStore.removeToken(token);
    }

    /**
     * 撤销 Refresh Token
     */
    public void revokeRefreshToken(String refreshToken) {
        tokenStore.removeRefreshToken(refreshToken);
    }

    /**
     * 撤销用户的所有 Token
     */
    public void revokeUserTokens(Long userId, String clientId) {
        tokenStore.revokeUserTokens(userId, clientId);
    }

    /**
     * 刷新 Token
     */
    public TokenInfo refreshToken(String refreshToken, int accessTokenTtl) {
        TokenStore.RefreshTokenInfo refreshInfo = tokenStore.findRefreshToken(refreshToken);
        if (refreshInfo == null) {
            return null;
        }

        // 撤销旧的 Access Token
        tokenStore.removeToken(refreshInfo.accessToken());

        // 生成新的 JWT Access Token
        TokenInfo newAccessToken = generateAccessToken(
                refreshInfo.userId(),
                null,
                refreshInfo.clientId(),
                refreshInfo.scopes(),
                accessTokenTtl
        );

        return newAccessToken;
    }

    /**
     * 生成不透明的 Token 字符串（Refresh Token、Authorization Code 使用）
     */
    private String generateTokenString() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
