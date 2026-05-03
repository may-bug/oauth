package org.codelin.oauth.oauth.authorization.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codelin.oauth.oauth.authorization.domain.model.OAuth2Client;
import org.codelin.oauth.oauth.authorization.infrastructure.token.TokenStore;
import org.codelin.oauth.oauth.common.security.TokenInfo;
import org.codelin.oauth.oauth.common.web.BizException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * OAuth2服务 - 授权码流程、客户端凭证流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final ClientService clientService;
    private final TokenService tokenService;

    /**
     * 生成授权码
     *
     * @param clientId    客户端ID
     * @param redirectUri 回调地址
     * @param scope       请求的scope（空格分隔）
     * @param state       状态参数
     * @param userId      授权用户ID
     * @return 授权码
     */
    public String generateAuthorizationCode(String clientId, String redirectUri,
                                             String scope, String state, Long userId) {
        // 验证客户端
        OAuth2Client client = clientService.getByClientId(clientId);
        if (client == null || client.getStatus() != 1) {
            throw BizException.badRequest("error.oauth.invalid-client");
        }

        // 验证回调地址
        List<String> allowedUris = clientService.getRedirectUris(clientId);
        if (!allowedUris.contains(redirectUri)) {
            throw BizException.badRequest("error.oauth.invalid-redirect-uri");
        }

        // 验证scope
        Set<String> scopes = parseScopes(scope);
        List<String> allowedScopes = clientService.getAllowedScopes(clientId);
        for (String s : scopes) {
            if (!allowedScopes.contains(s)) {
                throw BizException.badRequest("error.oauth.invalid-scope");
            }
        }

        // 生成授权码
        String code = tokenService.generateAuthorizationCode(
                clientId, userId, redirectUri, scopes, state);

        log.info("Authorization code generated for client: {}, user: {}", clientId, userId);
        return code;
    }

    /**
     * 用授权码交换Token
     *
     * @param code         授权码
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥（机密客户端需要）
     * @param redirectUri  回调地址
     * @return Token响应
     */
    public Map<String, Object> exchangeAuthorizationCode(String code, String clientId,
                                                          String clientSecret, String redirectUri) {
        // 验证客户端
        OAuth2Client client = clientService.getByClientId(clientId);
        if (client == null || client.getStatus() != 1) {
            throw BizException.badRequest("error.oauth.invalid-client");
        }

        // 机密客户端需要验证密钥
        if (client.getClientType() == 2) { // CONFIDENTIAL
            if (!clientService.validateSecret(clientId, clientSecret)) {
                throw BizException.badRequest("error.oauth.invalid-client-secret");
            }
        }

        // 消费授权码
        TokenStore.AuthorizationCodeInfo codeInfo = tokenService.consumeAuthorizationCode(code);
        if (codeInfo == null) {
            throw BizException.badRequest("error.oauth.invalid-grant");
        }

        // 验证clientId匹配
        if (!clientId.equals(codeInfo.clientId())) {
            throw BizException.badRequest("error.oauth.client-id-mismatch");
        }

        // 验证redirectUri匹配
        if (redirectUri != null && !redirectUri.equals(codeInfo.redirectUri())) {
            throw BizException.badRequest("error.oauth.redirect-uri-mismatch");
        }

        // 生成Token
        String scopeStr = codeInfo.scopes() != null ? String.join(" ", codeInfo.scopes()) : null;
        int accessTokenTtl = client.getAccessTokenTtl() != null ? client.getAccessTokenTtl() : 3600;
        int refreshTokenTtl = client.getRefreshTokenTtl() != null ? client.getRefreshTokenTtl() : 86400;

        TokenInfo accessToken = tokenService.generateAccessToken(
                codeInfo.userId(), null, clientId, codeInfo.scopes(), accessTokenTtl);
        String refreshToken = tokenService.generateRefreshToken(
                codeInfo.userId(), clientId, codeInfo.scopes(), accessToken.getToken());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", accessToken.getToken());
        response.put("token_type", "Bearer");
        response.put("expires_in", accessTokenTtl);
        response.put("refresh_token", refreshToken);
        if (scopeStr != null && !scopeStr.isEmpty()) {
            response.put("scope", scopeStr);
        }

        log.info("Authorization code exchanged for client: {}, user: {}", clientId, codeInfo.userId());
        return response;
    }

    /**
     * 客户端凭证流程
     *
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     * @param scope        请求的scope
     * @return Token响应
     */
    public Map<String, Object> clientCredentialsGrant(String clientId, String clientSecret,
                                                       String scope) {
        // 验证客户端
        if (!clientService.validateSecret(clientId, clientSecret)) {
            throw BizException.badRequest("error.oauth.client-auth-failed");
        }

        OAuth2Client client = clientService.getByClientId(clientId);
        if (client == null || client.getStatus() != 1) {
            throw BizException.badRequest("error.oauth.client-disabled");
        }

        // 验证scope
        Set<String> scopes = parseScopes(scope);
        List<String> allowedScopes = clientService.getAllowedScopes(clientId);
        for (String s : scopes) {
            if (!allowedScopes.contains(s)) {
                throw BizException.badRequest("error.oauth.invalid-scope");
            }
        }

        // 生成Token（客户端凭证流程没有用户）
        int accessTokenTtl = client.getAccessTokenTtl() != null ? client.getAccessTokenTtl() : 3600;
        TokenInfo accessToken = tokenService.generateAccessToken(
                null, null, clientId, scopes, accessTokenTtl);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", accessToken.getToken());
        response.put("token_type", "Bearer");
        response.put("expires_in", accessTokenTtl);
        if (scope != null && !scope.isEmpty()) {
            response.put("scope", scope);
        }

        log.info("Client credentials grant for client: {}", clientId);
        return response;
    }

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥（机密客户端需要）
     * @return Token响应
     */
    public Map<String, Object> refreshToken(String refreshToken, String clientId,
                                              String clientSecret) {
        // 验证客户端
        OAuth2Client client = clientService.getByClientId(clientId);
        if (client == null || client.getStatus() != 1) {
            throw BizException.badRequest("error.oauth.invalid-client");
        }

        // 机密客户端需要验证密钥
        if (client.getClientType() == 2) { // CONFIDENTIAL
            if (!clientService.validateSecret(clientId, clientSecret)) {
                throw BizException.badRequest("error.oauth.invalid-client-secret");
            }
        }

        // 验证刷新Token
        TokenStore.RefreshTokenInfo refreshTokenInfo = tokenService.validateRefreshToken(refreshToken);
        if (refreshTokenInfo == null) {
            throw BizException.badRequest("error.oauth.invalid-refresh-token");
        }

        // 验证clientId匹配
        if (!clientId.equals(refreshTokenInfo.clientId())) {
            throw BizException.badRequest("error.oauth.client-id-mismatch");
        }

        // 生成新的Token
        int accessTokenTtl = client.getAccessTokenTtl() != null ? client.getAccessTokenTtl() : 3600;
        int refreshTokenTtl = client.getRefreshTokenTtl() != null ? client.getRefreshTokenTtl() : 86400;

        TokenInfo newAccessToken = tokenService.generateAccessToken(
                refreshTokenInfo.userId(), null, clientId,
                refreshTokenInfo.scopes(), accessTokenTtl);
        String newRefreshToken = tokenService.generateRefreshToken(
                refreshTokenInfo.userId(), clientId,
                refreshTokenInfo.scopes(), newAccessToken.getToken());

        // 撤销旧的刷新Token
        tokenService.revokeRefreshToken(refreshToken);

        String scopeStr = refreshTokenInfo.scopes() != null ?
                String.join(" ", refreshTokenInfo.scopes()) : null;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", newAccessToken.getToken());
        response.put("token_type", "Bearer");
        response.put("expires_in", accessTokenTtl);
        response.put("refresh_token", newRefreshToken);
        if (scopeStr != null && !scopeStr.isEmpty()) {
            response.put("scope", scopeStr);
        }

        log.info("Token refreshed for client: {}, user: {}", clientId, refreshTokenInfo.userId());
        return response;
    }

    /**
     * 撤销Token
     *
     * @param token        要撤销的Token
     * @param tokenType    Token类型（access_token或refresh_token）
     * @param clientId     客户端ID
     * @param clientSecret 客户端密钥
     */
    public void revokeToken(String token, String tokenType, String clientId, String clientSecret) {
        // 验证客户端
        if (!clientService.validateSecret(clientId, clientSecret)) {
            throw BizException.badRequest("error.oauth.client-auth-failed");
        }

        if ("refresh_token".equals(tokenType)) {
            tokenService.revokeRefreshToken(token);
        } else {
            tokenService.revokeToken(token);
        }

        log.info("Token revoked for client: {}, type: {}", clientId, tokenType);
    }

    /**
     * 验证Token（用于资源服务器）
     *
     * @param accessToken 访问Token
     * @return Token信息
     */
    public Map<String, Object> introspect(String accessToken) {
        TokenInfo tokenInfo = tokenService.validateAccessToken(accessToken);

        Map<String, Object> response = new LinkedHashMap<>();
        if (tokenInfo != null) {
            response.put("active", true);
            response.put("client_id", tokenInfo.getClientId());
            response.put("username", tokenInfo.getUserId() != null ?
                    tokenInfo.getUserId().toString() : null);
            response.put("scope", tokenInfo.getScopes() != null ?
                    String.join(" ", tokenInfo.getScopes()) : null);
            response.put("exp", tokenInfo.getExpiresAt() != null ?
                    tokenInfo.getExpiresAt().getEpochSecond() : null);
        } else {
            response.put("active", false);
        }

        return response;
    }

    /**
     * 解析scope字符串为Set
     */
    private Set<String> parseScopes(String scope) {
        if (scope == null || scope.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(scope.split(" ")));
    }
}
