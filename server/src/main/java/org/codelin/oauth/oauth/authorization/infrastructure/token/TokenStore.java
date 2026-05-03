package org.codelin.oauth.oauth.authorization.infrastructure.token;

import org.codelin.oauth.oauth.common.security.TokenInfo;

import java.util.Set;

/**
 * Token存储接口
 */
public interface TokenStore {

    /**
     * 保存token
     */
    void saveToken(TokenInfo tokenInfo);

    /**
     * 查找token
     */
    TokenInfo findToken(String token);

    /**
     * 删除token
     */
    void removeToken(String token);

    /**
     * 撤销用户的所有token
     */
    void revokeUserTokens(Long userId, String clientId);

    /**
     * 保存授权码
     */
    void saveAuthorizationCode(String code, AuthorizationCodeInfo info);

    /**
     * 查找并删除授权码（一次性使用）
     */
    AuthorizationCodeInfo consumeAuthorizationCode(String code);

    /**
     * 保存Refresh Token
     */
    void saveRefreshToken(String refreshToken, RefreshTokenInfo info);

    /**
     * 查找Refresh Token
     */
    RefreshTokenInfo findRefreshToken(String refreshToken);

    /**
     * 删除Refresh Token
     */
    void removeRefreshToken(String refreshToken);

    /**
     * 授权码信息
     */
    record AuthorizationCodeInfo(
            String code,
            String clientId,
            Long userId,
            String redirectUri,
            Set<String> scopes,
            String state
    ) {}

    /**
     * Refresh Token信息
     */
    record RefreshTokenInfo(
            String token,
            String accessToken,
            String clientId,
            Long userId,
            Set<String> scopes
    ) {}
}
